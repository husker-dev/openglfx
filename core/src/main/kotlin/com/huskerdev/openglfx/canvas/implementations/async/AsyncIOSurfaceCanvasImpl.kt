package com.huskerdev.openglfx.canvas.implementations.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.Size

import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.iosurface.IOSurface
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class AsyncIOSurfaceCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): GLCanvas(GLInteropType.IOSurface, profile, flipY, msaa, true) {

    private val paintLock = Object()
    private val blitLock = Object()

    private var lastDrawSize = Size(-1, -1)
    private var lastResultSize = Size(-1, -1)

    private lateinit var ioSurface: IOSurface
    private lateinit var fxTexture: Texture

    private lateinit var sharedFboFX: Framebuffer
    private lateinit var sharedFboGL: Framebuffer
    private lateinit var fboFX: Framebuffer
    private lateinit var fboGL: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var fxContext: GLContext
    private lateinit var context: GLContext

    private var needsBlit = AtomicBoolean(false)

    private fun initializeThread(){
        fxContext = GLContext.current()
        thread {
            context = GLContext.create(0, profile == GLProfile.Core)
            context.makeCurrent()
            executor.initGLFunctions()

            while (!disposed){
                paint()
                synchronized(blitLock) {
                    fboGL.blitTo(sharedFboGL.id)
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    paintLock.wait()
                }
            }
        }
    }

    private fun paint(){
        lastDrawSize.onDifference(scaledWidth, scaledHeight){
            updateSurfaceSize(scaledWidth, scaledHeight)
            fireReshapeEvent(scaledWidth, scaledHeight)
        }

        glViewport(0, 0, lastDrawSize.width, lastDrawSize.height)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fboGL.id)
        if(msaa != 0)
            msaaFBO.blitTo(fboGL.id)
    }

    override fun onNGRender(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0)
            return

        if(!::context.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            synchronized(blitLock){
                lastResultSize.onDifference(scaledWidth, scaledHeight){
                    updateResultTextureSize(scaledWidth, scaledHeight)
                }
                glViewport(0, 0, lastResultSize.width, lastResultSize.height)

                sharedFboFX.blitTo(fboFX.id)
            }
        }
        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateSurfaceSize(width: Int, height: Int) {
        if (::ioSurface.isInitialized) {
            ioSurface.dispose()

            sharedFboGL.delete()
            fboGL.delete()
            if(msaa != 0) msaaFBO.delete()
        }
        ioSurface = IOSurface(width, height)

        // Create GL-side shared texture
        val ioGLTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, ioGLTexture)
        ioSurface.cglTexImageIOSurface2D(context, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        glBindTexture(GL_TEXTURE_RECTANGLE, 0)
        sharedFboGL = Framebuffer(width, height, existingTexture = ioGLTexture, existingTextureType = GL_TEXTURE_RECTANGLE)

        // Create simple framebuffer
        fboGL = Framebuffer(width, height)

        // Create multi-sampled framebuffer
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        } else fboGL.bindFramebuffer()
    }

    private fun updateResultTextureSize(width: Int, height: Int){
        if (::fxTexture.isInitialized) {
            fxTexture.dispose()

            fboFX.delete()
            sharedFboFX.delete()
        }

        // Create JavaFX texture
        fxTexture = GraphicsPipeline.getDefaultResourceFactory().createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()

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
        synchronized(paintLock){
            paintLock.notifyAll()
        }
        GLContext.delete(context)
        ioSurface.dispose()
        fxTexture.dispose()
    }
}