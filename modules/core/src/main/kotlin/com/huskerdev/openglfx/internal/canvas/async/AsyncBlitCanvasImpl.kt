package com.huskerdev.openglfx.internal.canvas.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.*
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.dispose
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.updateData
import com.huskerdev.openglfx.internal.PassthroughShader
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

open class AsyncBlitCanvasImpl(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): NGGLCanvas(canvas, executor, profile, flipY, msaa){

    private val paintLock = Object()
    private val blitLock = Object()

    private var needsBlit = AtomicBoolean(false)

    private var drawSize = Size()
    private var transferSize = Size()
    private var resultSize = Size()

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer
    private lateinit var transferFBO: Framebuffer
    private lateinit var resultFBO: Framebuffer

    private lateinit var context: GLContext
    private lateinit var resultContext: GLContext

    private lateinit var dataBuffer: ByteBuffer
    private lateinit var texture: Texture

    private lateinit var passthroughShader: PassthroughShader

    private fun initializeThread(){
        context = GLContext.create(0L, profile == GLProfile.Core)
        resultContext = GLContext.create(context, profile == GLProfile.Core)

        resultContext.makeCurrent()
        GLExecutor.loadBasicFunctionPointers()
        passthroughShader = PassthroughShader()

        thread(isDaemon = true) {
            context.makeCurrent()
            executor.initGLFunctions()
            canvas.fireInitEvent()

            while(!disposed){
                paint()
                synchronized(blitLock) {
                    transferSize.executeOnDifferenceWith(drawSize, ::resizeTransferFramebuffer)
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
                if (::texture.isInitialized) texture.dispose()
                if (::dataBuffer.isInitialized) dataBuffer.dispose()
                GLContext.delete(context)
                GLContext.delete(resultContext)
            }
        }
    }

    private fun paint(){
        drawSize.executeOnDifferenceWith(scaledSize, ::resizeDrawFramebuffer, canvas::fireReshapeEvent)

        glViewport(0, 0, drawSize.width, drawSize.height)
        canvas.fireRenderEvent(if (msaa != 0) msaaFBO.id else fbo.id)
        if (msaa != 0)
            msaaFBO.blitTo(fbo)
    }

    override fun renderContent(g: Graphics){
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if(!this::context.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            resultContext.makeCurrent()
            synchronized(blitLock){
                resultSize.executeOnDifferenceWith(transferSize) { width, height ->
                    resizeResultTexture(width, height)
                    glViewport(0, 0, width, height)
                }

                passthroughShader.copy(transferFBO, resultFBO)
                resultFBO.readPixels(0, 0, resultSize.width, resultSize.height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, dataBuffer)
                texture.updateData(dataBuffer, resultSize.width, resultSize.height)
            }
        }
        if(::texture.isInitialized)
            drawResultTexture(g, texture)
    }

    private fun resizeDrawFramebuffer(width: Int, height: Int) {
        if(::fbo.isInitialized){
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        fbo = Framebuffer(width, height)
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        } else fbo.bindFramebuffer()
    }

    private fun resizeTransferFramebuffer(width: Int, height: Int){
        if(::transferFBO.isInitialized)
            transferFBO.delete()
        transferFBO = Framebuffer(width, height)
    }

    private fun resizeResultTexture(width: Int, height: Int) {
        if(::dataBuffer.isInitialized) {
            dataBuffer.dispose()
            texture.dispose()
            resultFBO.delete()
        }
        dataBuffer = ByteBuffer.allocateDirect(width * height * 4)
        texture = GLFXUtils.createPermanentFXTexture(width, height)
        resultFBO = Framebuffer(width, height)
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