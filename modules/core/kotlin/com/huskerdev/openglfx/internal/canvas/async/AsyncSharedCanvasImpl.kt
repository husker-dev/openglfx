package com.huskerdev.openglfx.internal.canvas.async

import com.huskerdev.grapl.gl.GLContext
import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.*
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.shaders.PassthroughShader
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.shaders.FXAAShader
import com.sun.prism.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

open class AsyncSharedCanvasImpl(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile
): NGGLCanvas(canvas, executor, profile){

    private val paintLock = Object()
    private val blitLock = Object()

    private var drawSize = Size()
    private var transferSize = Size()
    private var resultSize = Size()

    private lateinit var context: GLContext
    private lateinit var fxContextWrapper: GLContext
    private lateinit var fxContext: GLContext

    private lateinit var fbo: Framebuffer
    private var msaaFBO: MultiSampledFramebuffer? = null
    private lateinit var transferFBO: Framebuffer
    private lateinit var resultFBO: Framebuffer

    private lateinit var fxTexture: Texture

    private var needsBlit = AtomicBoolean(false)

    private val passthroughShader by lazy { PassthroughShader() }
    private val fxaaShader by lazy { FXAAShader() }

    private fun initializeThread(){
        fxContext = GLContext.current()
        fxContextWrapper = GLContext.create(fxContext, profile)
        context = GLContext.create(fxContext, profile)

        thread(isDaemon = true) {
            context.makeCurrent()
            executor.initGLFunctions()

            while(!disposed){
                paint()
                synchronized(blitLock) {
                    transferSize.executeOnDifferenceWith(drawSize, ::updateTransferTextureSize)
                    fbo.blitTo(transferFBO)
                    glFinish()
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    if(!disposed) paintLock.wait()
                }
            }

            // Dispose
            canvas.fireDisposeEvent()
            GLContext.clear()
            GLFXUtils.runOnRenderThread {
                if(::fxTexture.isInitialized) fxTexture.dispose()

                if(::resultFBO.isInitialized) resultFBO.delete()
                if(::transferFBO.isInitialized) transferFBO.delete()
                if(::fbo.isInitialized) fbo.delete()
                msaaFBO?.delete()

                context.delete()
                fxContextWrapper.delete()
                fxContext.makeCurrent()
            }
        }
    }

    private fun paint(){
        if(drawSize != scaledSize ||
            msaa != (msaaFBO?.requestedSamples ?: 0)
        ){
            scaledSize.copyTo(drawSize)
            updateRenderFramebufferSize(drawSize.width, drawSize.height)
            canvas.fireReshapeEvent(drawSize.width, drawSize.height)
        }

        glViewport(0, 0, drawSize.width, drawSize.height)
        canvas.fireRenderEvent(msaaFBO?.id ?: fbo.id)
        msaaFBO?.blitTo(fbo)
    }

    override fun renderContent(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if (!::context.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            fxContextWrapper.makeCurrent()
            synchronized(blitLock){
                resultSize.executeOnDifferenceWith(transferSize) { width, height ->
                    updateResultFramebufferSize(width, height)
                    glViewport(0, 0, width, height)
                }
                (if(fxaa) fxaaShader else passthroughShader).apply(transferFBO, resultFBO)
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
            msaaFBO?.delete()
        }

        // Create framebuffer
        fbo = Framebuffer(width, height)

        // Create multi-sampled framebuffer
        if(msaa > 0){
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO!!.bindFramebuffer()
        } else {
            msaaFBO = null
            fbo.bindFramebuffer()
        }
    }

    override fun repaint() {
        synchronized(paintLock){
            paintLock.notifyAll()
        }
    }

    override fun timerTick() {
        if(needsBlit.get())
            dirty()
    }

    override fun dispose() {
        super.dispose()
        repaint()
    }
}