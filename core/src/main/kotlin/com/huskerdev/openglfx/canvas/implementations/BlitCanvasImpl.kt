package com.huskerdev.openglfx.canvas.implementations

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glReadPixels
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.OpenGLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.getPlatformImage
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.sun.javafx.geom.Rectangle
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.Image
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import sun.misc.Unsafe
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


open class BlitCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
) : OpenGLCanvas(GLInteropType.Blit, profile, flipY, msaa, false){

    companion object {
        private val bufferDirtyMethod = PixelBuffer::class.java.getDeclaredMethod("bufferDirty", Rectangle::class.java).apply { isAccessible = true }
        private fun PixelBuffer<*>.bufferDirty(rectangle: Rectangle?) = bufferDirtyMethod.invoke(this, rectangle)

        private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }[null] as Unsafe
    }

    private var initialized = false
    private var context: GLContext? = null

    private var image = WritableImage(1, 1)

    private var pixelByteBuffer: ByteBuffer? = null
    private lateinit var pixelBuffer: PixelBuffer<ByteBuffer>

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private var needsRepaint = AtomicBoolean(false)
    private var lastSize = Size(-1, -1)

    override fun onNGRender(g: Graphics){
        if(!initialized){
            initialized = true

            context = GLContext.create(0L, profile == GLProfile.Core)
            context!!.makeCurrent()
            executor.initGLFunctions()
            fireInitEvent()
        }
        context!!.makeCurrent()

        lastSize.onDifference(scaledWidth, scaledHeight){
            updateFramebufferSize(scaledWidth, scaledHeight)
            fireReshapeEvent(scaledWidth, scaledHeight)
        }
        glViewport(0, 0, lastSize.width, lastSize.height)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)

        if(msaa != 0)
            msaaFBO.blitTo(fbo.id)
        readPixels()

        val texture = g.resourceFactory.getCachedTexture(image.getPlatformImage() as Image, Texture.WrapMode.CLAMP_TO_EDGE)
        if(!texture.isLocked)
            texture.lock()

        drawResultTexture(g, texture)
        texture.unlock()
    }

    private fun readPixels() {
        if (scene == null || scene.window == null || width <= 0 || height <= 0)
            return

        val renderWidth = lastSize.width
        val renderHeight = lastSize.height
        if(renderWidth <= 0 || renderHeight <= 0)
            return

        if(image.width.toInt() != renderWidth || image.height.toInt() != renderHeight){
            if(pixelByteBuffer != null)
                unsafe.invokeCleaner(pixelByteBuffer!!)

            pixelByteBuffer = ByteBuffer.allocateDirect(renderWidth * renderHeight * 4)
            pixelBuffer = PixelBuffer(renderWidth, renderHeight, pixelByteBuffer!!, PixelFormat.getByteBgraPreInstance())

            image = WritableImage(pixelBuffer)
        }

        val oldDrawBuffer = GLExecutor.glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING)
        val oldReadBuffer = GLExecutor.glGetInteger(GL_READ_FRAMEBUFFER_BINDING)

        fbo.bindFramebuffer()
        glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelByteBuffer!!)

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oldDrawBuffer)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, oldReadBuffer)

        pixelBuffer.bufferDirty(null)
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
        if(::fbo.isInitialized){
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        fbo = Framebuffer(width, height)
        fbo.bindFramebuffer()

        if(msaa != 0) {
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
        unsafe.invokeCleaner(pixelByteBuffer!!)
        GLContext.delete(context!!)
    }
}