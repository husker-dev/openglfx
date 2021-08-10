package com.husker.openglfx

import com.husker.openglfx.utils.NodeUtils
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.*
import com.jogamp.opengl.GL2GL3.*
import javafx.animation.AnimationTimer
import javafx.scene.image.ImageView
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.layout.Pane
import jogamp.opengl.util.glsl.GLSLTextureRaster
import java.nio.IntBuffer
import kotlin.math.max


class OpenGLCanvas(capabilities: GLCapabilities, listener: GLEventListener, val fps: Int = 1000): Pane() {

    constructor(listener: GLEventListener): this(GLCapabilities(GLProfile.getDefault()), listener)

    private val glWindow: GLWindow
    private var imageView = ImageView()

    private var oldGLWidth = 0.0
    private var oldGLHeight = 0.0
    private var oldDPI = 0.0

    private val dpi: Double
        get() = scene.window.outputScaleX

    private lateinit var pixelIntBuffer: IntBuffer
    private lateinit var pixelBuffer: PixelBuffer<IntBuffer>
    private var renderWidth: Int = 0
    private var renderHeight: Int = 0

    private lateinit var glslTextureRaster: GLSLTextureRaster

    private var disposed = false

    init{
        imageView.fitWidthProperty().bind(widthProperty())
        imageView.fitHeightProperty().bind(heightProperty())

        imageView.isPreserveRatio = true
        children.add(imageView)

        capabilities.isFBO = true
        glWindow = GLWindow.create(capabilities)
        glWindow.addGLEventListener(object: GLEventListener{
            override fun init(drawable: GLAutoDrawable?) {
                val gl = drawable!!.gl
                glslTextureRaster = GLSLTextureRaster(0, true)
                glslTextureRaster.init(gl.gL2ES2)
                glslTextureRaster.reshape(gl.gL2ES2, 0, 0, width.toInt(), height.toInt())

                listener.init(drawable)
            }
            override fun dispose(drawable: GLAutoDrawable?) {
                listener.dispose(drawable)
            }
            override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {
                listener.reshape(drawable, x, y, width, height)
            }
            override fun display(drawable: GLAutoDrawable?) {
                listener.display(drawable!!)

                drawable.swapBuffers()
                glslTextureRaster.display(drawable.gl.gL2ES2)
                readGLPixels(drawable.gl as GL2)
            }
        })
        glWindow.isVisible = true

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(width > 0 && height > 0)
                    imageView.image = WritableImage(pixelBuffer)
            }
        }.start()

        NodeUtils.onWindowReady(this){ init() }
    }

    private fun init(){
        // Dispose listener
        scene.window.setOnCloseRequest { e ->
            dispose()
        }

        // FPS
        if(fps > 0){
            val sleep = (1000 / max(fps, 1000)).toLong()
            Thread{
                while(!disposed) {
                    Thread.sleep(sleep)
                    glWindow.display()
                }
            }.start()
        }

        // Resizing
        Thread{
            while(!disposed){
                Thread.sleep(1)
                if(oldGLWidth != width || oldGLHeight != height){
                    oldGLWidth = width
                    oldGLHeight = height
                    updateGLSize()
                }
                if(scene != null && scene.window != null && oldDPI != scene.window.outputScaleX){
                    oldDPI = scene.window.outputScaleX
                    updateGLSize()
                }
            }
        }.start()
    }

    fun dispose(){
        disposed = true
        glWindow.destroy()
    }

    private fun readGLPixels(gl: GL2){
        if (scene == null || scene.window == null || width <= 0 || height <= 0)
            return

        renderWidth = (width * dpi).toInt()
        renderHeight = (height * dpi).toInt()

        pixelIntBuffer = IntBuffer.allocate(renderWidth * renderHeight)
        gl.glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer)
        pixelBuffer = PixelBuffer(renderWidth, renderHeight, pixelIntBuffer, PixelFormat.getIntArgbPreInstance())
    }

    private fun updateGLSize(){
        glWindow.setSize(max(width * dpi, 100.0).toInt(), max(height * dpi, 100.0).toInt())
        glWindow.display()
    }
}