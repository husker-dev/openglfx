package com.huskerdev.openglfx.canvas.implementations.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.PassthroughShader
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
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
    private var interopTextureSize = Size(-1, -1)
    private var resultSize = Size(-1, -1)

    private lateinit var parallelContext: GLContext
    private lateinit var resultContext: GLContext
    private lateinit var fxContext: GLContext

    private lateinit var resultFBO: Framebuffer
    private lateinit var interThreadFBO: Framebuffer
    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var fxTexture: Texture

    private var needsBlit = AtomicBoolean(false)

    private lateinit var passthroughShader: PassthroughShader

    private fun initializeThread(){
        fxContext = GLContext.current()
        GLContext.clear()
        parallelContext = GLContext.create(fxContext, profile == GLProfile.Core)
        resultContext = GLContext.create(fxContext, profile == GLProfile.Core)
        fxContext.makeCurrent()

        thread(isDaemon = true) {
            parallelContext.makeCurrent()
            executor.initGLFunctions()

            while(!disposed){
                paint()
                synchronized(blitLock) {
                    interopTextureSize.changeOnDifference(drawSize){
                        updateInterTextureSize(sizeWidth, sizeHeight)
                    }
                    fbo.blitTo(interThreadFBO.id)
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    paintLock.wait()
                }
            }
        }
    }

    private fun paint(){
        drawSize.changeOnDifference(scaledWidth, scaledHeight) {
            updateFramebufferSize(scaledWidth, scaledHeight)
            fireReshapeEvent(scaledWidth, scaledHeight)
        }

        glViewport(0, 0, drawSize.sizeWidth, drawSize.sizeHeight)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)
        if(msaa != 0)
            msaaFBO.blitTo(fbo.id)
        glFinish()
    }

    override fun onNGRender(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0)
            return

        if (!::fxContext.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            resultContext.makeCurrent()

            if(!::passthroughShader.isInitialized)
                passthroughShader = PassthroughShader()

            resultSize.changeOnDifference(interopTextureSize) {
                updateResultFramebufferSize(sizeWidth, sizeHeight)
            }
            glViewport(0, 0, resultSize.sizeWidth, resultSize.sizeHeight)

            synchronized(blitLock){
                passthroughShader.copy(interThreadFBO, resultFBO)
            }
            fxContext.makeCurrent()
        }

        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateResultFramebufferSize(width: Int, height: Int) {
        if(::resultFBO.isInitialized)
            resultFBO.delete()

        // Create JavaFX texture
        if(::fxTexture.isInitialized)
            fxTexture.dispose()
        fxTexture = GraphicsPipeline.getDefaultResourceFactory()
            .createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()

        // Create framebuffer that connected to JavaFX's texture
        resultFBO = Framebuffer(width, height, existingTexture = fxTexture.GLTextureId)
    }

    private fun updateInterTextureSize(width: Int, height: Int) {
        if(::interThreadFBO.isInitialized)
            interThreadFBO.delete()
        interThreadFBO = Framebuffer(width, height)
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
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
        synchronized(paintLock){
            paintLock.notifyAll()
        }
        if(::fxTexture.isInitialized) fxTexture.dispose()
        if(::parallelContext.isInitialized) GLContext.delete(parallelContext)
        if(::resultContext.isInitialized) GLContext.delete(resultContext)
    }
}