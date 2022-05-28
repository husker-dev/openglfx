package com.huskerdev.openglfx.core.implementation

import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.core.*
import com.huskerdev.openglfx.utils.OpenGLFXUtils
import com.sun.javafx.geom.Rectangle
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.tk.PlatformImage
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Graphics
import com.sun.prism.Image
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.concurrent.atomic.AtomicBoolean

open class UniversalImpl(
    private val executor: GLExecutor,
    profile: Int
) : OpenGLCanvas(profile){

    companion object {
        private val bufferDirtyMethod = PixelBuffer::class.java.getDeclaredMethod("bufferDirty", Rectangle::class.java).apply { isAccessible = true }
        private fun PixelBuffer<*>.bufferDirty(rectangle: Rectangle?) = bufferDirtyMethod.invoke(this, rectangle)
    }

    private var initialized = false
    private var context: GLContext? = null

    private val removedBuffers = arrayListOf<Pair<ByteBuffer, Long>>()

    private var image = WritableImage(1, 1)

    private var pixelIntBuffer: IntBuffer? = null
    private var pixelByteBuffer: ByteBuffer? = null
    private lateinit var pixelBuffer: PixelBuffer<IntBuffer>

    private var texture = -1
    private var fbo = -1
    private var depthBuffer = -1

    private var needsRepaint = AtomicBoolean(false)
    private var lastSize = Pair(10, 10)

    init{
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                try {
                    // Garbage-collector for byte buffers
                    removedBuffers.removeAll {
                        return@removeAll if(System.nanoTime() - it.second > 1000000L * 1000 * 1){
                            OpenGLFXUtils.cleanByteBuffer(it.first)
                            true
                        } else false
                    }

                    if(needsRepaint.getAndSet(false)) {
                        NodeHelper.markDirty(this@UniversalImpl, DirtyBits.NODE_BOUNDS)
                        NodeHelper.markDirty(this@UniversalImpl, DirtyBits.REGION_SHAPE)
                    }
                } catch (_: Exception){}
            }
        }.start()
    }

    override fun onNGRender(g: Graphics){
        if(!initialized){
            initialized = true

            context = GLContext.createNew(executor, profile)
            context!!.makeCurrent()
            executor.initGLFunctions()

            fireInitEvent()
        }
        context!!.makeCurrent()

        if(scaledWidth.toInt() != lastSize.first || scaledHeight.toInt() != lastSize.second){
            lastSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
            updateFramebufferSize()
            fireReshapeEvent(lastSize.first, lastSize.second)
        }

        executor.glViewport(0, 0, lastSize.first, lastSize.second)
        fireRenderEvent()
        readPixels()

        val texture = g.resourceFactory.getCachedTexture(image.getPlatformImage() as Image, Texture.WrapMode.CLAMP_TO_EDGE)
        if(!texture.isLocked)
            texture.lock()
        drawResultTexture(g, texture)
        texture.unlock()
    }

    private fun readPixels() = executor.run {
        if (scene == null || scene.window == null || width <= 0 || height <= 0)
            return

        val renderWidth = lastSize.first
        val renderHeight = lastSize.second
        if(renderWidth <= 0 || renderHeight <= 0)
            return

        if(image.width.toInt() != renderWidth || image.height.toInt() != renderHeight){
            if(pixelByteBuffer != null)
                removedBuffers.add(pixelByteBuffer!! to System.nanoTime())

            pixelByteBuffer = ByteBuffer.allocateDirect(renderWidth * renderHeight * Int.SIZE_BYTES)
            pixelIntBuffer = pixelByteBuffer!!.asIntBuffer()
            pixelBuffer = PixelBuffer(renderWidth, renderHeight, pixelIntBuffer, PixelFormat.getIntArgbPreInstance())

            image = WritableImage(pixelBuffer)
        }

        glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer!!)
        pixelBuffer.bufferDirty(null)
        return@run
    }

    private fun updateFramebufferSize() = executor.run {
        if(texture != -1)
            glDeleteTextures(texture)
        if(fbo != -1)
            glDeleteFramebuffers(fbo)
        if(depthBuffer != -1)
            glDeleteRenderbuffers(depthBuffer)

        fbo = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)

        texture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, lastSize.first, lastSize.second, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0)

        depthBuffer = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, lastSize.first, lastSize.second)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer)
    }

    override fun repaint() = needsRepaint.set(true)

    private fun WritableImage.getPlatformImage() = Toolkit.getImageAccessor().getPlatformImage(this) as PlatformImage
}