package com.huskerdev.openglfx.canvas.implementations.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.Size

import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.iosurface.IOSurface
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

open class AsyncIOSurfaceCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): GLCanvas(GLInteropType.IOSurface, profile, flipY, msaa, true) {

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
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var fxContext: GLContext
    private lateinit var fxWrapperContext: GLContext
    private lateinit var context: GLContext

    private var needsBlit = AtomicBoolean(false)

    private fun initializeThread(){
        fxContext = GLContext.current()
        fxWrapperContext = GLContext.create(fxContext, false)
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
            fireDisposeEvent()
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
        drawSize.executeOnDifferenceWith(scaledSize, ::updateFramebufferSize, ::fireReshapeEvent)

        glViewport(0, 0, drawSize.width, drawSize.height)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fboGL.id)
        if(msaa != 0)
            msaaFBO.blitTo(fboGL)
    }

    override fun onNGRender(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if(!::fxContext.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            synchronized(blitLock){
                resultSize.executeOnDifferenceWith(interopTextureSize, ::updateResultTextureSize)
                fxWrapperContext.makeCurrent()
                glViewport(0, 0, scaledWidth, scaledHeight)
                sharedFboFX.blitTo(fboFX)
                fxContext.makeCurrent()
            }
        }
        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
        if (::fboGL.isInitialized) {
            fboGL.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        // Create simple framebuffer
        fboGL = Framebuffer(width, height)

        // Create multi-sampled framebuffer
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        } else fboGL.bindFramebuffer()
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
        if (::fxTexture.isInitialized) {
            fxTexture.dispose()

            fboFX.delete()
            sharedFboFX.delete()
        }

        // Create JavaFX texture
        fxTexture = GLFXUtils.createPermanentFXTexture(width, height)

        // Create FX-side shared texture
        val ioFXTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, ioFXTexture)
        ioSurface.cglTexImageIOSurface2D(fxContext, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0)
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
            markDirty()
    }

    override fun dispose() {
        super.dispose()
        repaint()
    }
}