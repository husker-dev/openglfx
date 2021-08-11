package com.husker.openglfx

import com.husker.openglfx.utils.NodeUtils
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.*
import com.jogamp.opengl.GL2GL3.*
import com.jogamp.opengl.util.FPSAnimator
import com.sun.javafx.geom.Rectangle
import javafx.application.Platform
import javafx.scene.image.*
import javafx.scene.layout.Pane
import jogamp.opengl.util.glsl.GLSLTextureRaster
import java.nio.IntBuffer
import kotlin.math.max


open class OpenGLCanvas(capabilities: GLCapabilities, listener: GLEventListener, val targetFPS: Int = 60): Pane() {

    constructor(listener: GLEventListener): this(GLCapabilities(GLProfile.getDefault()), listener)

    val glWindow: GLWindow
    private lateinit var glslTextureRaster: GLSLTextureRaster

    private var imageView = ImageView()
    private var image = WritableImage(1, 1)

    /**
     * Uses instead of [PixelBuffer.updateBuffer], because it requires JavaFX thread.
     * Despite the fact that this is Reflection, the performance is noticeably improved.
     **/
    private var bufferDirtyMethod = WritableImage::class.java.getDeclaredMethod("bufferDirty", Rectangle::class.java)

    private val dpi: Double
        get() = scene.window.outputScaleX

    private lateinit var pixelIntBuffer: IntBuffer
    private lateinit var pixelBuffer: PixelBuffer<IntBuffer>

    init{
        bufferDirtyMethod.isAccessible = true

        imageView.fitWidthProperty().bind(widthProperty())
        imageView.fitHeightProperty().bind(heightProperty())
        children.add(imageView)

        widthProperty().addListener{_, _, _ -> updateGLSize() }
        heightProperty().addListener{_, _, _ -> updateGLSize() }

        capabilities.isFBO = true
        glWindow = GLWindow.create(capabilities)
        glWindow.addGLEventListener(listener)
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
                drawable!!.swapBuffers()
                glslTextureRaster.display(drawable.gl.gL2ES2)
                readGLPixels(drawable.gl as GL2)
            }
        })
        glWindow.isVisible = true

        NodeUtils.onWindowReady(this){ onWindowReady() }
    }

    private fun onWindowReady(){
        val oldOnCloseRequest = scene.window.onCloseRequest
        scene.window.setOnCloseRequest {
            glWindow.animator.stop()
            glWindow.destroy()
            oldOnCloseRequest?.handle(it)
        }

        if(targetFPS > 0) {
            glWindow.animator = FPSAnimator(glWindow, targetFPS)
            glWindow.animator.start()
        }
    }

    private fun readGLPixels(gl: GL2){
        if (scene == null || scene.window == null || width <= 0 || height <= 0)
            return

        val renderWidth = (width * dpi).toInt()
        val renderHeight = (height * dpi).toInt()

        if(image.width.toInt() != renderWidth || image.height.toInt() != renderHeight){
            pixelIntBuffer = IntBuffer.allocate(renderWidth * renderHeight)
            pixelBuffer = PixelBuffer(renderWidth, renderHeight, pixelIntBuffer, PixelFormat.getIntArgbPreInstance())
            image = WritableImage(pixelBuffer)
            Platform.runLater { imageView.image = image }
        }

        gl.glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer)
        bufferDirtyMethod.invoke(image, null)
    }

    private fun updateGLSize() = glWindow.setSize(max(width * dpi, 10.0).toInt(), max(height * dpi, 10.0).toInt())

}