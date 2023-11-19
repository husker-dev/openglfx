package com.huskerdev.openglfx.canvas.implementations.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glReadPixels
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.OpenGLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.getPlatformImage
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.PassthroughShader
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
import kotlin.concurrent.thread

class AsyncBlitCanvasImpl(
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

    private val paintLock = Object()
    private val blitLock = Object()

    private var needsBlit = AtomicBoolean(false)

    private var lastDrawSize = Pair(-1, -1)
    private var lastResultSize = Pair(-1, -1)

    private lateinit var resultFBO: Framebuffer
    private lateinit var interThreadFBO: Framebuffer
    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var context: GLContext
    private lateinit var resultContext: GLContext

    private var pixelByteBuffer: ByteBuffer? = null
    private lateinit var pixelBuffer: PixelBuffer<ByteBuffer>
    private var image = WritableImage(1, 1)

    private lateinit var passthroughShader: PassthroughShader

    init{
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsBlit.get()) {
                    NodeHelper.markDirty(this@AsyncBlitCanvasImpl, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@AsyncBlitCanvasImpl, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()
    }

    private fun initializeThread(){
        context = GLContext.create(0L, profile == GLProfile.Core)
        resultContext = GLContext.create(context, profile == GLProfile.Core)
        resultContext.makeCurrent()
        executor.initGLFunctions()

        thread(isDaemon = true) {
            context.makeCurrent()
            executor.initGLFunctions()
            fireInitEvent()

            while(!disposed){
                paint()
                synchronized(blitLock) {
                    fbo.blitTo(interThreadFBO.id)
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    paintLock.wait()
                }
            }
        }
    }

    private fun paint(){
        if (scaledWidth.toInt() != lastDrawSize.first || scaledHeight.toInt() != lastDrawSize.second) {
            lastDrawSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
            updateFramebufferSize()

            fireReshapeEvent(lastDrawSize.first, lastDrawSize.second)
        }

        glViewport(0, 0, lastDrawSize.first, lastDrawSize.second)
        fireRenderEvent(if (msaa != 0) msaaFBO.id else fbo.id)
        if (msaa != 0)
            msaaFBO.blitTo(fbo.id)
    }

    override fun onNGRender(g: Graphics){
        if(!this::context.isInitialized)
            initializeThread()

        if(width == 0.0 || height == 0.0)
            return

        if (needsBlit.getAndSet(false)) {
            synchronized(blitLock){
                if(!::passthroughShader.isInitialized)
                    passthroughShader = PassthroughShader()

                if (scaledWidth.toInt() != lastResultSize.first || scaledHeight.toInt() != lastResultSize.second) {
                    lastResultSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
                    updateResultTextureSize()
                }

                passthroughShader.copy(interThreadFBO, resultFBO)

                glViewport(0, 0, lastResultSize.first, lastResultSize.second)
                glReadPixels(0, 0, lastResultSize.first, lastResultSize.second, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelByteBuffer!!)
                pixelBuffer.bufferDirty(null)

                val texture = g.resourceFactory.getCachedTexture(image.getPlatformImage() as Image, Texture.WrapMode.CLAMP_TO_EDGE)
                if(!texture.isLocked)
                    texture.lock()
                drawResultTexture(g, texture)
                texture.unlock()
            }
        }
    }

    private fun updateResultTextureSize(){
        if(::resultFBO.isInitialized)
            resultFBO.delete()

        val width = lastResultSize.first
        val height = lastResultSize.second

        if(pixelByteBuffer != null)
            unsafe.invokeCleaner(pixelByteBuffer!!)

        pixelByteBuffer = ByteBuffer.allocateDirect(width * height * 4)
        pixelBuffer = PixelBuffer(width, height, pixelByteBuffer!!, PixelFormat.getByteBgraPreInstance())
        image = WritableImage(pixelBuffer)

        resultFBO = Framebuffer(width, height)
    }

    private fun updateFramebufferSize() {
        if(::fbo.isInitialized){
            interThreadFBO.delete()
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        val width = lastDrawSize.first
        val height = lastDrawSize.second

        interThreadFBO = Framebuffer(width, height)

        fbo = Framebuffer(width, height)
        fbo.bindFramebuffer()

        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        }
    }

    override fun repaint() {
        synchronized(paintLock){
            paintLock.notifyAll()
        }
    }

    override fun dispose() {
        super.dispose()
        unsafe.invokeCleaner(pixelByteBuffer!!)
        GLContext.delete(context)
        GLContext.delete(resultContext)
    }
}