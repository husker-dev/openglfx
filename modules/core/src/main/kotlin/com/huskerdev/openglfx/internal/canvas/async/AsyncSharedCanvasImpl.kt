package com.huskerdev.openglfx.internal.canvas.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.*
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.PassthroughShader
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.sun.prism.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

open class AsyncSharedCanvasImpl(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): NGGLCanvas(canvas, executor, profile, flipY, msaa){

    private val paintLock = Object()
    private val blitLock = Object()

    private var drawSize = Size()
    private var transferSize = Size()
    private var resultSize = Size()

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
                    transferSize.executeOnDifferenceWith(drawSize, ::updateTransferTextureSize)
                    fbo.blitTo(transferFBO)
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    if(!disposed) paintLock.wait()
                }
            }

            // Dispose
            GLContext.clear()
            GLFXUtils.runOnRenderThread {
                if(::fxTexture.isInitialized) fxTexture.dispose()

                if(::resultFBO.isInitialized) resultFBO.delete()
                if(::transferFBO.isInitialized) transferFBO.delete()
                if(::fbo.isInitialized) fbo.delete()
                if(::msaaFBO.isInitialized) msaaFBO.delete()

                GLContext.delete(context)
                GLContext.delete(fxWrapperContext)
            }
        }
    }

    private fun paint(){
        drawSize.executeOnDifferenceWith(scaledSize, ::updateRenderFramebufferSize, canvas::fireReshapeEvent)

        glViewport(0, 0, drawSize.width, drawSize.height)
        canvas.fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)
        if(msaa != 0)
            msaaFBO.blitTo(fbo)
        glFinish()
    }

    override fun renderContent(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if (!::context.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            fxWrapperContext.makeCurrent()

            resultSize.executeOnDifferenceWith(transferSize, ::updateResultFramebufferSize)
            glViewport(0, 0, resultSize.width, resultSize.height)

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
            fxTexture.dispose()
        }

        // Create JavaFX texture
        fxTexture = GLFXUtils.createPermanentFXTexture(width, height)

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