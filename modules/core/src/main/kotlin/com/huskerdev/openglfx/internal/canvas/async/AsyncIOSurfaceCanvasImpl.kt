package com.huskerdev.openglfx.internal.canvas.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.internal.Size

import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.iosurface.IOSurface
import com.huskerdev.openglfx.internal.shaders.FXAAShader
import com.huskerdev.openglfx.internal.shaders.PassthroughShader
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

open class AsyncIOSurfaceCanvasImpl(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile
): NGGLCanvas(canvas, executor, profile) {

    private val paintLock = Object()
    private val blitLock = Object()

    private var drawSize = Size()
    private var interopTextureSize = Size()
    private var resultSize = Size()

    private lateinit var ioSurface: IOSurface
    private lateinit var fxTexture: Texture

    private lateinit var sharedFboFX: Framebuffer
    private lateinit var sharedFboGL: Framebuffer
    private lateinit var fboFX: Framebuffer
    private lateinit var fboGL: Framebuffer
    private var msaaFBO: MultiSampledFramebuffer? = null

    private lateinit var fxContext: GLContext
    private lateinit var fxContextWrapper: GLContext
    private lateinit var context: GLContext

    private var needsBlit = AtomicBoolean(false)

    private val fxaaShader by lazy { FXAAShader() }

    private fun initializeThread(){
        fxContext = GLContext.current()
        fxContextWrapper = GLContext.create(fxContext, false)

        thread(isDaemon = true) {
            context = GLContext.create(0, profile == GLProfile.Core)
            context.makeCurrent()
            executor.initGLFunctions()

            while (!disposed){
                paint()
                synchronized(blitLock) {
                    interopTextureSize.executeOnDifferenceWith(drawSize, ::updateSurfaceSize)
                    fboGL.blitTo(sharedFboGL)
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    paintLock.wait()
                }
            }

            // Dispose
            canvas.fireDisposeEvent()
            GLContext.clear()
            GLFXUtils.runOnRenderThread {
                if(::sharedFboFX.isInitialized) sharedFboFX.delete()
                if(::fboFX.isInitialized) fboFX.delete()

                if(::fxTexture.isInitialized) fxTexture.dispose()
                if(::ioSurface.isInitialized) ioSurface.dispose()

                if(::context.isInitialized) GLContext.delete(context)
            }
        }
    }

    private fun paint(){
        if(drawSize != scaledSize ||
            msaa != (msaaFBO?.requestedSamples ?: 0)
        ){
            scaledSize.copyTo(drawSize)
            updateFramebufferSize(drawSize.width, drawSize.height)
            canvas.fireReshapeEvent(drawSize.width, drawSize.height)
        }

        glViewport(0, 0, drawSize.width, drawSize.height)
        canvas.fireRenderEvent(msaaFBO?.id ?: fboGL.id)
        msaaFBO?.blitTo(fboGL)
    }

    override fun renderContent(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if(!::fxContext.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            synchronized(blitLock){
                resultSize.executeOnDifferenceWith(interopTextureSize) { width, height ->
                    updateResultTextureSize(width, height)
                    fxContextWrapper.makeCurrent()
                    glViewport(0, 0, width, height)
                }
                fxContextWrapper.makeCurrent()
                // We can't skip this blit, so blit at first, then apply shader
                sharedFboFX.blitTo(fboFX)
                if(fxaa) fxaaShader.apply(fboFX, fboFX)
                glFinish()
            }
            fxContext.makeCurrent()
        }
        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
        if (::fboGL.isInitialized) {
            fboGL.delete()
            msaaFBO?.delete()
        }

        // Create simple framebuffer
        fboGL = Framebuffer(width, height)

        // Create multi-sampled framebuffer
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO?.bindFramebuffer()
        } else {
            msaaFBO = null
            fboGL.bindFramebuffer()
        }
    }

    private fun updateSurfaceSize(width: Int, height: Int){
        if (::ioSurface.isInitialized) {
            sharedFboGL.delete()
            ioSurface.dispose()
        }
        ioSurface = IOSurface(width, height)

        // Create GL-side shared texture
        val ioGLTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, ioGLTexture)
        ioSurface.cglTexImageIOSurface2D(context, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        glBindTexture(GL_TEXTURE_RECTANGLE, 0)
        sharedFboGL = Framebuffer(width, height, existingTexture = ioGLTexture, existingTextureType = GL_TEXTURE_RECTANGLE)
    }

    private fun updateResultTextureSize(width: Int, height: Int){
        // Create JavaFX texture
        if (::fxTexture.isInitialized)
            fxTexture.dispose()
        fxTexture = GLFXUtils.createPermanentFXTexture(width, height)

        fxContextWrapper.makeCurrent()
        if(::fboFX.isInitialized){
            fboFX.delete()
            sharedFboFX.delete()
        }

        // Create FX-side shared texture
        val ioFXTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, ioFXTexture)
        ioSurface.cglTexImageIOSurface2D(fxContextWrapper, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        glBindTexture(GL_TEXTURE_RECTANGLE, 0)

        // Create JavaFX buffers
        sharedFboFX = Framebuffer(width, height, existingTexture = ioFXTexture, existingTextureType = GL_TEXTURE_RECTANGLE)
        fboFX = Framebuffer(width, height, existingTexture = fxTexture.GLTextureId)
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