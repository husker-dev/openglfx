package com.huskerdev.openglfx.canvas.implementations.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.disposeManually
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.PassthroughShader
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.sun.prism.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

open class AsyncSharedCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): GLCanvas(GLInteropType.TextureSharing, profile, flipY, msaa, true){

    private val paintLock = Object()
    private val blitLock = Object()

    private var drawSize = Size(-1, -1)
    private var transferSize = Size(-1, -1)
    private var resultSize = Size(-1, -1)

    private lateinit var context: GLContext
    private lateinit var fxWrapperContext: GLContext
    private lateinit var fxContext: GLContext

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer
    private lateinit var transferFBO: Framebuffer
    private lateinit var resultFBO: Framebuffer

    private lateinit var fxTexture: Texture

    private var needsBlit = AtomicBoolean(false)

    private lateinit var passthroughShader: PassthroughShader

    private fun initializeThread(){
        fxContext = GLContext.current()
        GLContext.clear()
        context = GLContext.create(fxContext, profile == GLProfile.Core)

        fxWrapperContext = GLContext.create(fxContext, profile == GLProfile.Core)
        fxWrapperContext.makeCurrent()
        GLExecutor.loadBasicFunctionPointers()
        passthroughShader = PassthroughShader()

        fxContext.makeCurrent()

        thread(isDaemon = true) {
            context.makeCurrent()
            executor.initGLFunctions()

            while(!disposed){
                paint()
                synchronized(blitLock) {
                    transferSize.changeOnDifference(drawSize){
                        updateTransferTextureSize(sizeWidth, sizeHeight)
                    }
                    fbo.blitTo(transferFBO.id)
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    if(!disposed) paintLock.wait()
                }
            }

            if(::fxTexture.isInitialized) fxTexture.disposeManually(false)
            if(::resultFBO.isInitialized) resultFBO.delete()
            if(::transferFBO.isInitialized) transferFBO.delete()
            if(::fbo.isInitialized) fbo.delete()
            if(::msaaFBO.isInitialized) msaaFBO.delete()
            GLContext.delete(context)
            GLContext.delete(fxWrapperContext)
        }
    }

    private fun paint(){
        drawSize.changeOnDifference(scaledWidth, scaledHeight) {
            updateRenderFramebufferSize(scaledWidth, scaledHeight)
            fireReshapeEvent(scaledWidth, scaledHeight)
        }

        glViewport(0, 0, drawSize.sizeWidth, drawSize.sizeHeight)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)
        if(msaa != 0)
            msaaFBO.blitTo(fbo.id)
        glFinish()
    }

    override fun onNGRender(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if (!::context.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            fxWrapperContext.makeCurrent()

            resultSize.changeOnDifference(transferSize) {
                updateResultFramebufferSize(sizeWidth, sizeHeight)
            }
            glViewport(0, 0, resultSize.sizeWidth, resultSize.sizeHeight)

            synchronized(blitLock){
                passthroughShader.copy(transferFBO, resultFBO)
            }
            fxContext.makeCurrent()
        }

        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateResultFramebufferSize(width: Int, height: Int) {
        if(::resultFBO.isInitialized) {
            resultFBO.delete()
            fxTexture.disposeManually()
        }

        // Create JavaFX texture
        fxTexture = GraphicsPipeline.getDefaultResourceFactory()
            .createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()

        // Create framebuffer that connected to JavaFX's texture
        resultFBO = Framebuffer(width, height, existingTexture = fxTexture.GLTextureId)
    }

    private fun updateTransferTextureSize(width: Int, height: Int) {
        if(::transferFBO.isInitialized)
            transferFBO.delete()
        transferFBO = Framebuffer(width, height)
    }

    private fun updateRenderFramebufferSize(width: Int, height: Int) {
        if(::fbo.isInitialized){
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        // Create framebuffer
        fbo = Framebuffer(width, height)
        fbo.bindFramebuffer()

        // Create multi-sampled framebuffer
        if(msaa != 0){
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        }
    }

    override fun repaint() {
        synchronized(paintLock){
            paintLock.notifyAll()
        }
    }

    override fun timerTick() {
        if(needsBlit.get())
            markDirty()
    }

    override fun dispose() {
        super.dispose()
        repaint()
    }
}