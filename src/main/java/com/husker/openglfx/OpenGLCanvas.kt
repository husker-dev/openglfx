package com.husker.openglfx

import com.husker.openglfx.utils.NodeUtils
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.*
import com.jogamp.opengl.GL2GL3.GL_BGRA
import com.jogamp.opengl.GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV
import javafx.animation.AnimationTimer
import javafx.scene.image.ImageView
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.layout.Pane
import jogamp.opengl.util.glsl.GLSLTextureRaster
import java.nio.IntBuffer
import kotlin.math.max


class OpenGLCanvas(capabilities: GLCapabilities, listener: GLEventListener, val fps: Int = 1000): Pane() {

    constructor(listener: GLEventListener): this(GLCapabilities(GLProfile.getDefault()), listener)

    private val glWindow: GLWindow
    private var canvas = ImageView()
    private var image = WritableImage(1, 1)

    private var oldGLWidth = 0.0
    private var oldGLHeight = 0.0
    private var oldDPI = 0.0

    private lateinit var pixelIntBuffer: IntBuffer
    private var renderWidth: Int = 0
    private var renderHeight: Int = 0

    private lateinit var glslTextureRaster: GLSLTextureRaster

    private var disposed = false

    private enum class RenderState{
        GRAB_GL,
        DRAW_FX
    }
    private var renderingState = RenderState.GRAB_GL

    init{
        capabilities.isFBO = true
        glWindow = GLWindow.create(capabilities)

        canvas.isPreserveRatio = true
        children.add(canvas)

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
                if(renderingState == RenderState.GRAB_GL)
                    return

                if(renderingState == RenderState.DRAW_FX){
                    try {
                        if(image.width.toInt() != renderWidth || image.height.toInt() != renderHeight) {
                            image = WritableImage(renderWidth, renderHeight)
                            canvas.image = image
                            canvas.fitWidth = width
                            canvas.fitHeight = height
                        }
                        image.pixelWriter.setPixels(0, 0, renderWidth, renderHeight, PixelFormat.getIntArgbInstance(), pixelIntBuffer.array(), 0, renderWidth)
                    }finally {
                        renderingState = RenderState.GRAB_GL
                    }
                }
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
        if (renderingState != RenderState.GRAB_GL || scene == null || scene.window == null)
            return

        if (width <= 0 || height <= 0)
            return

        val dpi = scene.window.outputScaleX
        renderWidth = (width * dpi).toInt()
        renderHeight = (height * dpi).toInt()

        pixelIntBuffer = IntBuffer.allocate(renderWidth * renderHeight)
        gl.glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer)

        renderingState = RenderState.DRAW_FX
    }

    private fun updateGLSize(){
        val dpi = scene.window.outputScaleX
        val width = if(width > 0) (width * dpi) else 300.0
        val height = if(height > 0) (height * dpi) else 300.0

        glWindow.setSize(width.toInt(), height.toInt())
        glWindow.display()
    }
}