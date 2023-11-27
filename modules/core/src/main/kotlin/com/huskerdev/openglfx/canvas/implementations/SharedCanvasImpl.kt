package com.huskerdev.openglfx.canvas.implementations

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean

open class SharedCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): GLCanvas(GLInteropType.TextureSharing, profile, flipY, msaa, false){

    private var lastSize = Size()

    private lateinit var context: GLContext
    private lateinit var fxContext: GLContext

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var fxTexture: Texture

    private var needsRepaint = AtomicBoolean(false)

    override fun onNGRender(g: Graphics) {
        if (!::context.isInitialized) {
            fxContext = GLContext.current()
            context = GLContext.create(fxContext, profile == GLProfile.Core)
            executor.initGLFunctions()
        }
        context.makeCurrent()

        lastSize.executeOnDifferenceWith(scaledSize, ::updateFramebufferSize, ::fireReshapeEvent)

        glViewport(0, 0, lastSize.width, lastSize.height)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)
        if(msaa != 0)
            msaaFBO.blitTo(fbo)

        glFinish()
        fxContext.makeCurrent()

        drawResultTexture(g, fxTexture)
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
        if(::fbo.isInitialized){
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        fxContext.makeCurrent()
        // Create JavaFX texture
        if(::fxTexture.isInitialized)
            fxTexture.dispose()
        fxTexture = GLFXUtils.createPermanentFXTexture(width, height)
        context.makeCurrent()

        // Create framebuffer that connected to JavaFX's texture
        fbo = Framebuffer(width, height, existingTexture = fxTexture.GLTextureId)
        fbo.bindFramebuffer()

        // Create multi-sampled framebuffer
        if(msaa != 0){
            msaaFBO = MultiSampledFramebuffer(msaa, lastSize.width, lastSize.height)
            msaaFBO.bindFramebuffer()
        }
    }

    override fun repaint() = needsRepaint.set(true)

    override fun timerTick() {
        if(needsRepaint.getAndSet(false))
            markDirty()
    }

    override fun dispose() {
        super.dispose()
        GLFXUtils.runOnRenderThread {
            context.makeCurrent()
            fireDisposeEvent()
            fxContext.makeCurrent()
            if(::fxTexture.isInitialized) fxTexture.dispose()
            if(::context.isInitialized) GLContext.delete(context)
        }
    }
}