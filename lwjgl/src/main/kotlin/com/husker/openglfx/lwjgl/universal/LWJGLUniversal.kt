package com.husker.openglfx.lwjgl.universal

import com.husker.openglfx.lwjgl.LWJGLCanvas
import com.husker.openglfx.utils.FXUtils
import com.husker.openglfx.utils.LifetimeLoopThread
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.tk.PlatformImage
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Graphics
import com.sun.prism.Image
import com.sun.prism.Texture
import com.sun.prism.impl.BufferUtil
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import java.nio.IntBuffer
import kotlin.concurrent.thread
import kotlin.math.max

class LWJGLUniversal: LWJGLCanvas() {

    private val resizeUpdating = LifetimeLoopThread(100){ updateGLSize() }
    private var bufferUpdateRequired = false

    private var image = WritableImage(1, 1)
    private var lastImage = image
    private var imageReady = false

    private lateinit var pixelIntBuffer: IntBuffer
    private lateinit var pixelBuffer: PixelBuffer<IntBuffer>

    private var window = 0L

    private var shouldPaint = Object()
    private var lastSize = Pair(10, 10)
    private var initialized = false

    init{
        widthProperty().addListener{_, _, _ ->
            resizeUpdating.startRequest()
            synchronized(shouldPaint) { shouldPaint.notifyAll() }
        }
        heightProperty().addListener{_, _, _ ->
            resizeUpdating.startRequest()
            synchronized(shouldPaint) { shouldPaint.notifyAll() }
        }

        Platform.runLater {
            GLFWErrorCallback.createPrint(System.err).set()
            if (!glfwInit())
                throw IllegalStateException("Unable to initialize GLFW")

            glfwDefaultWindowHints()
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)

            window = glfwCreateWindow(lastSize.first, lastSize.second, "", 0, 0)
            if (window == 0L)
                throw RuntimeException("Failed to create the GLFW window")

            thread(isDaemon = true){
                glfwMakeContextCurrent(window)
                GL.createCapabilities()

                while(!glfwWindowShouldClose(window)){
                    if(width.toInt() != lastSize.first || height.toInt() != lastSize.second){
                        updateGLSize()
                        lastSize = Pair(width.toInt(), height.toInt())

                        if(!initialized){
                            initialized = true
                            fireInitEvent()
                        }
                        fireReshapeEvent()
                    }

                    glViewport(0, 0, (width * dpi).toInt(), (height * dpi).toInt())
                    fireRenderEvent()

                    readGLPixels()
                    NodeHelper.markDirty(this@LWJGLUniversal, DirtyBits.NODE_GEOMETRY)

                    synchronized(shouldPaint) { shouldPaint.wait() }
                }
            }
        }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                try {
                    if (bufferUpdateRequired) {
                        pixelBuffer.updateBuffer { null }
                        bufferUpdateRequired = false
                    }
                } catch (_: Exception){}
            }
        }.start()
        FXUtils.onWindowReady(this){ onWindowReady() }
    }

    private fun onWindowReady(){
        val oldOnCloseRequest = scene.window.onCloseRequest
        scene.window.setOnCloseRequest {
            glfwDestroyWindow(window)
            oldOnCloseRequest?.handle(it)
        }

        // DPI changed listener
        var lastDPI = 1.0
        val windowMovingListener = {
            if(dpi != lastDPI) {
                lastDPI = dpi
                updateGLSize()
            }
        }
        scene.xProperty().addListener{_, _, _ -> windowMovingListener()}
        scene.yProperty().addListener{_, _, _ -> windowMovingListener()}
    }

    private fun readGLPixels(){
        if (scene == null || scene.window == null || width <= 0 || height <= 0)
            return

        val renderWidth = (width * dpi).toInt()
        val renderHeight = (height * dpi).toInt()
        if(renderWidth <= 0 || renderHeight <= 0)
            return

        if(image.width.toInt() != renderWidth || image.height.toInt() != renderHeight){
            pixelIntBuffer = BufferUtil.newIntBuffer(renderWidth * renderHeight)
            pixelBuffer = PixelBuffer(renderWidth, renderHeight, pixelIntBuffer, PixelFormat.getIntArgbPreInstance())

            image = WritableImage(pixelBuffer)
            imageReady = false
        }
        glReadBuffer(1)
        glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer)
        imageReady = true
        bufferUpdateRequired = true
    }

    private fun updateGLSize() {
        if(window != 0L)
            glfwSetWindowSize(window, max(width * dpi, 1.0).toInt(), max(height * dpi, 1.0).toInt())
    }

    override fun onNGRender(g: Graphics){
        val imageToRender = if(imageReady){
            lastImage = image
            image
        }else lastImage

        val texture = g.resourceFactory.getCachedTexture(imageToRender.getPlatformImage() as Image, Texture.WrapMode.CLAMP_TO_EDGE)
        if(!texture.isLocked)
            texture.lock()

        g.drawTexture(texture,
            0f, 0f, imageToRender.width.toFloat(), imageToRender.height.toFloat(),
            0f, 0f, imageToRender.width.toFloat() * dpi.toFloat(), imageToRender.height.toFloat() * dpi.toFloat())
        texture.unlock()
    }

    override fun repaint() {
        synchronized(shouldPaint) { shouldPaint.notifyAll() }
    }

    private fun WritableImage.getPlatformImage() = Toolkit.getImageAccessor().getPlatformImage(this) as PlatformImage
}