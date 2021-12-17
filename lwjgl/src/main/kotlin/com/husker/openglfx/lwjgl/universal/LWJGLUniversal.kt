package com.husker.openglfx.lwjgl.universal

import com.husker.openglfx.lwjgl.LWJGLCanvas
import com.husker.openglfx.utils.FXUtils
import com.husker.openglfx.utils.LifetimeLoopThread
import com.sun.javafx.geom.Matrix3f
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.impl.BufferUtil
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.scene.image.ImageView
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.transform.Transform
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

    private var imageView = ImageView()
    private var image = WritableImage(1, 1)

    private lateinit var pixelIntBuffer: IntBuffer
    private lateinit var pixelBuffer: PixelBuffer<IntBuffer>

    private var window = 0L

    private var shouldPaint = Object()
    private var lastSize = Pair(10, 10)
    private var initialized = false

    init{
        imageView.fitWidthProperty().bind(widthProperty())
        imageView.fitHeightProperty().bind(heightProperty())
        children.add(imageView)

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

                var tex = 0
                var texFBO = 0

                while(!glfwWindowShouldClose(window)){
                    if(width.toInt() != lastSize.first || height.toInt() != lastSize.second){
                        updateGLSize()
                        lastSize = Pair(width.toInt(), height.toInt())

                        /*
                        // Gen FBO
                        texFBO = glGenFramebuffers()
                        glBindFramebuffer(GL_FRAMEBUFFER, texFBO)

                        // Gen Texture
                        tex = glGenTextures()
                        glBindTexture(GL_TEXTURE_2D, tex)
                        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.toInt(), height.toInt(), 0, GL_RGB, GL_UNSIGNED_BYTE, 0)

                         */

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
            Platform.runLater { imageView.image = image }
        }
        glReadBuffer(1)
        glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer)
        bufferUpdateRequired = true
    }

    private fun updateGLSize() {
        if(window != 0L)
            glfwSetWindowSize(window, max(width * dpi, 1.0).toInt(), max(height * dpi, 1.0).toInt())
    }

    override fun onNGRender(g: Graphics){}

    override fun repaint() {
        synchronized(shouldPaint) { shouldPaint.notifyAll() }
    }
}