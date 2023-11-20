package com.huskerdev.openglfx.canvas.implementations.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.getPlatformImage
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.sun.javafx.geom.Rectangle
import com.sun.prism.Graphics
import com.sun.prism.Image
import com.sun.prism.Texture
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import sun.misc.Unsafe
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

internal class AsyncBlitCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
) : GLCanvas(GLInteropType.Blit, profile, flipY, msaa, true){

    companion object {
        private val bufferDirtyMethod = PixelBuffer::class.java.getDeclaredMethod("bufferDirty", Rectangle::class.java).apply { isAccessible = true }
        private fun PixelBuffer<*>.bufferDirty(rectangle: Rectangle?) = bufferDirtyMethod.invoke(this, rectangle)

        private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }[null] as Unsafe
    }

    private val paintLock = Object()
    private val blitLock = Object()

    private var needsBlit = AtomicBoolean(false)

    private var lastDrawSize = Size(-1, -1)
    private var lastResultSize = Size(-1, -1)

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var context: GLContext

    private var pixelByteBuffer: ByteBuffer? = null
    private lateinit var pixelBuffer: PixelBuffer<ByteBuffer>
    private lateinit var image: WritableImage

    private fun initializeThread(){
        thread(isDaemon = true) {
            context = GLContext.create(0L, profile == GLProfile.Core)
            context.makeCurrent()
            executor.initGLFunctions()
            fireInitEvent()

            while(!disposed){
                paint()
                synchronized(blitLock) {
                    lastResultSize.onDifference(lastDrawSize.width, lastDrawSize.height){
                        updateResultTextureSize(lastDrawSize.width, lastDrawSize.height)
                    }
                    fbo.readPixels(0, 0, lastResultSize.width, lastResultSize.height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelByteBuffer!!)
                    pixelBuffer.bufferDirty(null)
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    paintLock.wait()
                }
            }
        }
    }

    private fun paint(){
        lastDrawSize.onDifference(scaledWidth, scaledHeight) {
            updateFramebufferSize(scaledWidth, scaledHeight)
            fireReshapeEvent(scaledWidth, scaledHeight)
        }

        glViewport(0, 0, lastDrawSize.width, lastDrawSize.height)
        fireRenderEvent(if (msaa != 0) msaaFBO.id else fbo.id)
        if (msaa != 0)
            msaaFBO.blitTo(fbo.id)
    }

    override fun onNGRender(g: Graphics){
        if(scaledWidth == 0 || scaledHeight == 0)
            return

        if(!this::context.isInitialized)
            initializeThread()

        if (needsBlit.getAndSet(false)) {
            synchronized(blitLock){
                val texture = g.resourceFactory.getCachedTexture(image.getPlatformImage() as Image, Texture.WrapMode.CLAMP_TO_EDGE)
                if(!texture.isLocked)
                    texture.lock()
                drawResultTexture(g, texture)
                texture.unlock()
            }
        }
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
        if(::fbo.isInitialized){
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        fbo = Framebuffer(width, height)
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        }else fbo.bindFramebuffer()
    }

    private fun updateResultTextureSize(width: Int, height: Int) {
        if(pixelByteBuffer != null)
            unsafe.invokeCleaner(pixelByteBuffer!!)
        pixelByteBuffer = ByteBuffer.allocateDirect(width * height * 4)
        pixelBuffer = PixelBuffer(width, height, pixelByteBuffer!!, PixelFormat.getByteBgraPreInstance())
        image = WritableImage(pixelBuffer)
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
        unsafe.invokeCleaner(pixelByteBuffer!!)
        GLContext.delete(context)
    }
}