package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
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
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean

open class IOSurfaceCanvasImpl(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): NGGLCanvas(canvas, executor, profile, flipY, msaa) {

    private lateinit var ioSurface: IOSurface
    private lateinit var fxTexture: Texture

    private val lastSize = Size(minWidth = 1, minHeight = 1)

    private lateinit var fboFX: Framebuffer
    private lateinit var sharedFboFX: Framebuffer
    private lateinit var sharedFboGL: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var fxContext: GLContext
    private lateinit var fxWrapperContext: GLContext
    private lateinit var context: GLContext

    private var needsRepaint = AtomicBoolean(false)

    override fun renderContent(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if(!::context.isInitialized){
            fxContext = GLContext.current()
            fxWrapperContext = GLContext.create(fxContext, false)
            context = GLContext.create(0, profile == GLProfile.Core)
            context.makeCurrent()
            executor.initGLFunctions()
        }
        context.makeCurrent()

        lastSize.executeOnDifferenceWith(scaledSize, ::updateFramebufferSize, canvas::fireReshapeEvent)

        glViewport(0, 0, lastSize.width, lastSize.height)
        canvas.fireRenderEvent(if(msaa != 0) msaaFBO.id else sharedFboGL.id)
        if(msaa != 0)
            msaaFBO.blitTo(sharedFboGL)

        fxWrapperContext.makeCurrent()
        glViewport(0, 0, lastSize.width, lastSize.height)
        sharedFboFX.blitTo(fboFX)
        fxContext.makeCurrent()

        drawResultTexture(g, fxTexture)
    }

    private fun updateFramebufferSize(width: Int, height: Int){
        if (::sharedFboGL.isInitialized) {
            ioSurface.dispose()
            fxTexture.dispose()

            fboFX.delete()
            sharedFboFX.delete()
            sharedFboGL.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        ioSurface = IOSurface(width, height)

        // Create JavaFX texture
        fxContext.makeCurrent()
        fxTexture = GLFXUtils.createPermanentFXTexture(width, height)

        // Create FX-side shared texture
        val ioFXTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, ioFXTexture)
        ioSurface.cglTexImageIOSurface2D(fxContext, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        glBindTexture(GL_TEXTURE_RECTANGLE, 0)

        // Create JavaFX buffers
        sharedFboFX = Framebuffer(width, height, existingTexture = ioFXTexture, existingTextureType = GL_TEXTURE_RECTANGLE)
        fboFX = Framebuffer(width, height, existingTexture = fxTexture.GLTextureId)

        // Create GL-side shared texture
        context.makeCurrent()
        val ioGLTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, ioGLTexture)
        ioSurface.cglTexImageIOSurface2D(context, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        glBindTexture(GL_TEXTURE_RECTANGLE, 0)

        // Create GL buffers
        sharedFboGL = Framebuffer(width, height, existingTexture = ioGLTexture, existingTextureType = GL_TEXTURE_RECTANGLE)

        // Create multi-sampled framebuffer
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        } else sharedFboGL.bindFramebuffer()
    }

    override fun repaint() = needsRepaint.set(true)

    override fun timerTick() {
        if(needsRepaint.getAndSet(false))
            dirty()
    }

    override fun dispose() {
        super.dispose()
        GLFXUtils.runOnRenderThread {
            context.makeCurrent()
            canvas.fireDisposeEvent()
            fxContext.makeCurrent()

            if(::sharedFboFX.isInitialized) sharedFboFX.delete()
            if(::fboFX.isInitialized) fboFX.delete()

            if(::fxTexture.isInitialized) fxTexture.dispose()
            if(::ioSurface.isInitialized) ioSurface.dispose()

            if(::context.isInitialized) GLContext.delete(context)
        }
    }
}