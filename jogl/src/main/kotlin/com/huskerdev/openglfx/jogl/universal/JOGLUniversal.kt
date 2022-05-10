package com.huskerdev.openglfx.jogl.universal

import com.huskerdev.openglfx.jogl.JOGLFXCanvas
import com.huskerdev.openglfx.utils.OpenGLFXUtils
import com.jogamp.opengl.*
import com.jogamp.opengl.GL2GL3.*
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.tk.PlatformImage
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Graphics
import com.sun.prism.Image
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import jogamp.opengl.GLDrawableFactoryImpl
import jogamp.opengl.GLOffscreenAutoDrawableImpl
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class JOGLUniversal(
    capabilities: GLCapabilities
): JOGLFXCanvas() {

    private val removedBuffers = arrayListOf<Pair<ByteBuffer, Long>>()

    private var bufferUpdateRequired = false

    private var image = WritableImage(1, 1)
    private var lastImage = image
    private var imageReady = false

    private var pixelIntBuffer: IntBuffer? = null
    private var pixelByteBuffer: ByteBuffer? = null
    private lateinit var pixelBuffer: PixelBuffer<IntBuffer>

    private var texture = -1
    private var textureFBO = -1

    private var needsRepaint = AtomicBoolean(false)
    private var repaintLock = Object()

    private var lastSize = Pair(-1, -1)
    private var initialized = false

    init{
        OpenGLFXUtils.executeOnMainThread {
            capabilities.isFBO = true
            val glWindow = GLDrawableFactoryImpl
                .getFactoryImpl(capabilities.glProfile)
                .createOffscreenAutoDrawable(GLProfile.getDefaultDevice(), capabilities, null, 100, 100) as GLOffscreenAutoDrawableImpl
            glWindow.display()

            thread(isDaemon = true) {
                glWindow.context.makeCurrent()
                val gl = glWindow.gl

                while(true){
                    renderThread = Thread.currentThread()
                    if(scaledWidth.toInt() != lastSize.first || scaledHeight.toInt() != lastSize.second){
                        lastSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
                        updateFramebufferSize(gl)

                        if(!initialized){
                            initialized = true
                            fireInitEvent(gl)
                        }
                        fireReshapeEvent(gl, lastSize.first, lastSize.second)
                    }

                    gl.glViewport(0, 0, lastSize.first, lastSize.second)
                    fireRenderEvent(gl)

                    readPixels(gl as GL2)

                    needsRepaint.set(true)
                    synchronized(repaintLock) { repaintLock.wait() }
                }
            }
        }

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

                    if (bufferUpdateRequired) {
                        pixelBuffer.updateBuffer { null }
                        bufferUpdateRequired = false
                    }
                    if(needsRepaint.getAndSet(false)) {
                        NodeHelper.markDirty(this@JOGLUniversal, DirtyBits.NODE_BOUNDS)
                        NodeHelper.markDirty(this@JOGLUniversal, DirtyBits.REGION_SHAPE)
                    }
                } catch (_: Exception){}
            }
        }.start()
    }

    private fun readPixels(gl: GL2){
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
            imageReady = false
        }

        gl.glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer)
        imageReady = true
        bufferUpdateRequired = true
    }

    private fun updateFramebufferSize(gl: GL) {
        val buffer = intArrayOf(0)
        if(texture != -1)
            gl.glDeleteTextures(1, intArrayOf(texture), 0)
        if(textureFBO != -1)
            gl.glDeleteFramebuffers(1, intArrayOf(textureFBO), 0)

        gl.glGenTextures(1, buffer, 0)
        texture = buffer[0]
        gl.glBindTexture(GL_TEXTURE_2D, texture)
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, lastSize.first, lastSize.second, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)

        gl.glGenFramebuffers(1, buffer, 0)
        textureFBO = buffer[0]
        gl.glBindFramebuffer(GL_FRAMEBUFFER, textureFBO)
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0)
    }

    override fun onNGRender(g: Graphics){
        if(!initialized)
            return

        val imageToRender = if(imageReady){
            lastImage = image
            image
        }else lastImage

        val texture = g.resourceFactory.getCachedTexture(imageToRender.getPlatformImage() as Image, Texture.WrapMode.CLAMP_TO_EDGE)
        if(!texture.isLocked)
            texture.lock()

        g.drawTexture(texture,
            0f, 0f, scaledWidth.toFloat(), scaledHeight.toFloat(),
            0f, 0f, imageToRender.width.toFloat() * dpi.toFloat(), imageToRender.height.toFloat() * dpi.toFloat())
        texture.unlock()
    }

    override fun repaint() {
        if(isVisible)
            synchronized(repaintLock) { repaintLock.notifyAll() }
    }

    private fun WritableImage.getPlatformImage() = Toolkit.getImageAccessor().getPlatformImage(this) as PlatformImage
}