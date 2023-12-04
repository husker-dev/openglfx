package com.huskerdev.openglfx.internal.canvas

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
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean

open class IOSurfaceCanvasImpl(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile
): NGGLCanvas(canvas, executor, profile) {

    private lateinit var ioSurface: IOSurface
    private lateinit var fxTexture: Texture

    private val drawSize = Size()

    private lateinit var fboFX: Framebuffer
    private lateinit var sharedFboFX: Framebuffer
    private lateinit var sharedFboGL: Framebuffer
    private var msaaFBO: MultiSampledFramebuffer? = null

    private lateinit var fxContext: GLContext
    private lateinit var fxWrapperContext: GLContext
    private lateinit var context: GLContext

    private var needsRepaint = AtomicBoolean(false)

    private val fxaaShader by lazy { FXAAShader() }

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

        if(drawSize != scaledSize ||
            msaa != (msaaFBO?.requestedSamples ?: 0)
        ){
            scaledSize.copyTo(drawSize)
            updateFramebufferSize(drawSize.width, drawSize.height)
            canvas.fireReshapeEvent(drawSize.width, drawSize.height)
        }

        glViewport(0, 0, drawSize.width, drawSize.height)
        canvas.fireRenderEvent(msaaFBO?.id ?: sharedFboGL.id)
        msaaFBO?.blitTo(sharedFboGL)
        glFinish()

        fxWrapperContext.makeCurrent()
        glViewport(0, 0, drawSize.width, drawSize.height)
        sharedFboFX.blitTo(fboFX)
        if(fxaa) fxaaShader.apply(fboFX, fboFX)
        glFinish()
        fxContext.makeCurrent()

        drawResultTexture(g, fxTexture)
    }

    private fun updateFramebufferSize(width: Int, height: Int){
        if(::ioSurface.isInitialized)
            ioSurface.dispose()
        ioSurface = IOSurface(width, height)

        // Create JavaFX texture
        fxContext.makeCurrent()
        if(::fxTexture.isInitialized)
            fxTexture.dispose()
        fxTexture = GLFXUtils.createPermanentFXTexture(width, height)

        fxWrapperContext.makeCurrent()
        if(::fboFX.isInitialized){
            fboFX.delete()
            sharedFboFX.delete()
        }

        // Create FX-side shared texture
        val ioFXTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, ioFXTexture)
        ioSurface.cglTexImageIOSurface2D(fxWrapperContext, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        glBindTexture(GL_TEXTURE_RECTANGLE, 0)

        // Create JavaFX buffers
        sharedFboFX = Framebuffer(width, height, existingTexture = ioFXTexture, existingTextureType = GL_TEXTURE_RECTANGLE)
        fboFX = Framebuffer(width, height, existingTexture = fxTexture.GLTextureId)

        // Create GL-side shared texture
        context.makeCurrent()
        if(::sharedFboGL.isInitialized){
            sharedFboGL.delete()
            msaaFBO?.delete()
        }

        val ioGLTexture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, ioGLTexture)
        ioSurface.cglTexImageIOSurface2D(context, GL_TEXTURE_RECTANGLE, GL_RGBA, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0)
        glBindTexture(GL_TEXTURE_RECTANGLE, 0)

        // Create GL buffers
        sharedFboGL = Framebuffer(width, height, existingTexture = ioGLTexture, existingTextureType = GL_TEXTURE_RECTANGLE)

        // Create multi-sampled framebuffer
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO?.bindFramebuffer()
        } else {
            msaaFBO = null
            sharedFboGL.bindFramebuffer()
        }
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