package com.husker.openglfx

import com.husker.openglfx.utils.LifetimeLoopThread
import com.husker.openglfx.utils.NodeUtils
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.*
import com.jogamp.opengl.GL2GL3.*
import com.jogamp.opengl.util.FPSAnimator
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.scene.image.*
import javafx.scene.layout.Pane
import jogamp.opengl.util.glsl.GLSLTextureRaster
import java.nio.IntBuffer
import kotlin.math.max


open class OpenGLCanvas @JvmOverloads constructor(
        private var initGLEventListener: GLEventListener,
        private var capabilities: GLCapabilities,
        val targetFPS: Int = 60
    ): Pane() {

    private lateinit var glWindow: GLWindow
    private lateinit var glslTextureRaster: GLSLTextureRaster
    var glEventListener: GLEventListener
        set(value) {
            if(this::glWindow.isInitialized) {
                glWindow.removeGLEventListener(glWindow.getGLEventListener(0))
                glWindow.addGLEventListener(0, value)
            }else
                initGLEventListener = value
        }
        get() {
            return if(this::glWindow.isInitialized)
                glWindow.getGLEventListener(0)
            else
                initGLEventListener
        }

    private val resizeUpdating = LifetimeLoopThread(200){ updateGLSize() }
    private var bufferUpdateRequired = false

    private var imageView = ImageView()
    private var image = WritableImage(1, 1)

    private val dpi: Double
        get() = scene.window.outputScaleX

    private lateinit var pixelIntBuffer: IntBuffer
    private lateinit var pixelBuffer: PixelBuffer<IntBuffer>

    @JvmOverloads constructor(targetFPS: Int = 60): this(EmptyGLEventListener, GLCapabilities(GLProfile.getDefault()), targetFPS)
    @JvmOverloads constructor(listener: GLEventListener, targetFPS: Int = 60): this(listener, GLCapabilities(GLProfile.getDefault()), targetFPS)
    @JvmOverloads constructor(capabilities: GLCapabilities, targetFPS: Int = 60): this(EmptyGLEventListener, capabilities, targetFPS)

    init{
        imageView.fitWidthProperty().bind(widthProperty())
        imageView.fitHeightProperty().bind(heightProperty())
        children.add(imageView)

        widthProperty().addListener{_, _, _ -> resizeUpdating.startRequest() }
        heightProperty().addListener{_, _, _ -> resizeUpdating.startRequest() }

        Thread{
            capabilities.isFBO = true
            glWindow = GLWindow.create(capabilities)
            glWindow.addGLEventListener(initGLEventListener)
            glWindow.addGLEventListener(object: GLEventListener{
                override fun init(drawable: GLAutoDrawable?) {
                    val gl = drawable!!.gl
                    glslTextureRaster = GLSLTextureRaster(0, true)
                    glslTextureRaster.init(gl.gL2ES2)
                    glslTextureRaster.reshape(gl.gL2ES2, 0, 0, width.toInt(), height.toInt())
                }
                override fun dispose(drawable: GLAutoDrawable?) {}
                override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {}
                override fun display(drawable: GLAutoDrawable?) {
                    readGLPixels(drawable!!, drawable.gl as GL2)
                }
            })
            glWindow.isVisible = true

            NodeUtils.onWindowReady(this){ onWindowReady() }
        }.start()

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(bufferUpdateRequired) {
                    pixelBuffer.updateBuffer { null }
                    bufferUpdateRequired = false
                }
            }
        }.start()
    }

    private fun onWindowReady(){
        val oldOnCloseRequest = scene.window.onCloseRequest
        scene.window.setOnCloseRequest {
            glWindow.animator.stop()
            glWindow.destroy()
            oldOnCloseRequest?.handle(it)
        }

        var lastDPI = 1.0
        val windowMovingListener = {
            if(dpi != lastDPI) {
                lastDPI = dpi
                updateGLSize()
            }
        }
        scene.xProperty().addListener{_, _, _ -> windowMovingListener()}
        scene.yProperty().addListener{_, _, _ -> windowMovingListener()}

        if(targetFPS > 0) {
            glWindow.animator = FPSAnimator(glWindow, targetFPS, true)
            glWindow.animator.start()
        }
    }

    private fun readGLPixels(drawable: GLAutoDrawable, gl: GL2){
        if (scene == null || scene.window == null || width <= 0 || height <= 0)
            return

        drawable.swapBuffers()
        glslTextureRaster.display(gl.gL2ES2)

        val renderWidth = (width * dpi).toInt()
        val renderHeight = (height * dpi).toInt()
        if(renderWidth <= 0 || renderHeight <= 0)
            return

        if(image.width.toInt() != renderWidth || image.height.toInt() != renderHeight){
            pixelIntBuffer = IntBuffer.allocate(renderWidth * renderHeight)
            pixelBuffer = PixelBuffer(renderWidth, renderHeight, pixelIntBuffer, PixelFormat.getIntArgbPreInstance())
            image = WritableImage(pixelBuffer)
            Platform.runLater { imageView.image = image }
        }

        gl.glReadBuffer(gl.defaultReadBuffer)
        gl.glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer)
        bufferUpdateRequired = true
    }

    private fun updateGLSize() = glWindow.setSize(max(width * dpi, 1.0).toInt(), max(height * dpi, 1.0).toInt())

    private object EmptyGLEventListener: GLEventListener{
        override fun init(drawable: GLAutoDrawable?) {}
        override fun dispose(drawable: GLAutoDrawable?) {}
        override fun display(drawable: GLAutoDrawable?) {}
        override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {}
    }
}