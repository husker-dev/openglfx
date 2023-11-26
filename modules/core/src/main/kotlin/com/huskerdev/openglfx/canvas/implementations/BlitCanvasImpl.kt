package com.huskerdev.openglfx.canvas.implementations

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.dispose
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.updateData
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


open class BlitCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
) : GLCanvas(GLInteropType.Blit, profile, flipY, msaa, false){

    private var needsRepaint = AtomicBoolean(false)

    private var resultSize = Size()

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var context: GLContext

    private lateinit var dataBuffer: ByteBuffer
    private lateinit var texture: Texture

    override fun onNGRender(g: Graphics){
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if(!::context.isInitialized){
            context = GLContext.create(0L, profile == GLProfile.Core)
            context.makeCurrent()
            executor.initGLFunctions()
            fireInitEvent()
        }
        context.makeCurrent()

        resultSize.executeOnDifferenceWith(scaledSize){ width, height ->
            resizeTextures(width, height)
            fireReshapeEvent(width, height)
            glViewport(0, 0, resultSize.width, resultSize.height)
        }
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)

        if(msaa != 0)
            msaaFBO.blitTo(fbo)
        fbo.readPixels(0, 0, resultSize.width, resultSize.height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, dataBuffer)
        texture.updateData(dataBuffer, resultSize.width, resultSize.height)

        drawResultTexture(g, texture)
    }

    private fun resizeTextures(width: Int, height: Int) {
        if(::fbo.isInitialized){
            texture.dispose()
            dataBuffer.dispose()
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        fbo = Framebuffer(width, height)
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, resultSize.width, resultSize.height)
            msaaFBO.bindFramebuffer()
        } else fbo.bindFramebuffer()

        dataBuffer = ByteBuffer.allocateDirect(width * height * 4)
        texture = GLFXUtils.createPermanentFXTexture(width, height)
    }

    override fun repaint() = needsRepaint.set(true)

    override fun timerTick() {
        if(needsRepaint.getAndSet(false))
            markDirty()
    }

    override fun dispose() {
        super.dispose()
        GLFXUtils.runOnRenderThread {
            if(::dataBuffer.isInitialized) dataBuffer.dispose()
            if(::texture.isInitialized) texture.dispose()
            if(::context.isInitialized) GLContext.delete(context)
        }
    }
}