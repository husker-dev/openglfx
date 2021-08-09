package com.husker.openglfx

import com.husker.openglfx.utils.NodeUtils
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.*
import com.jogamp.opengl.GL.*
import javafx.animation.AnimationTimer
import javafx.scene.image.ImageView
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.layout.Pane
import jogamp.newt.OffscreenWindow
import jogamp.opengl.util.glsl.GLSLTextureRaster
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.roundToInt


class OpenGLCanvas(capabilities: GLCapabilities, listener: GLEventListener, val fps: Int = 1000): Pane() {

    constructor(listener: GLEventListener): this(GLCapabilities(GLProfile.getDefault()), listener)

    private val glWindow: GLWindow
    private var canvas = ImageView()

    private val rgbaBuffers = arrayListOf<ByteBuffer>()

    private var oldGLWidth = 0.0
    private var oldGLHeight = 0.0
    private var oldDPI = 0.0

    private val chunkSize = 512  // px
    private var chunksW = 0
    private var chunksH = 0
    private var image = WritableImage(1, 1)

    private var disposed = false

    private enum class RenderState{
        GRAB_GL,
        DRAW_FX
    }
    private var renderingState = RenderState.GRAB_GL

    init{
        capabilities.isOnscreen = false
        glWindow = GLWindow.create(capabilities)

        canvas.isPreserveRatio = true
        children.add(canvas)

        glWindow.addGLEventListener(listener)
        glWindow.addGLEventListener(object: GLEventListener{
            override fun init(drawable: GLAutoDrawable?) {}
            override fun dispose(drawable: GLAutoDrawable?) {}
            override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {}

            override fun display(drawable: GLAutoDrawable?) {
                drawable!!.swapBuffers()
                updateGL(drawable.gl as GL2)
            }
        })

        glWindow.isVisible = true

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(renderingState == RenderState.GRAB_GL)
                    return

                if(renderingState == RenderState.DRAW_FX){
                    val imageWidth = chunksW * chunkSize
                    val imageHeight = chunksH * chunkSize

                    if (image.width.toInt() != imageWidth || image.height.toInt() != imageHeight) {
                        image = WritableImage(imageWidth, imageHeight)
                        canvas.image = image
                    }

                    canvas.fitWidth = image.width / scene.window.outputScaleX
                    canvas.fitHeight = image.height / scene.window.outputScaleX

                    val writer = image.pixelWriter

                    for (i in 0 until chunksW)
                        for (r in 0 until chunksH)
                            writer.setPixels(i * chunkSize, r * chunkSize, chunkSize, chunkSize, PixelFormat.getByteRgbInstance(), rgbaBuffers[r * chunksW + i].array(), 0, chunkSize * 3)

                    renderingState = RenderState.GRAB_GL
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
                    glWindow.display()
                }
                if(scene != null && scene.window != null && oldDPI != scene.window.outputScaleX){
                    oldDPI = scene.window.outputScaleX
                    updateGLSize()
                    glWindow.display()
                }
            }
        }.start()
    }

    fun dispose(){
        disposed = true
        glWindow.destroy()
    }

    private fun updateGL(gl: GL2){
        if (renderingState != RenderState.GRAB_GL || scene == null || scene.window == null)
            return

        val dpi = scene.window.outputScaleX
        val renderWidth = (width * dpi).toInt()
        val renderHeight = (height * dpi).toInt()

        if (width <= 0 || height <= 0)
            return

        chunksW = (renderWidth.toDouble() / chunkSize.toDouble() + 0.5).roundToInt()
        chunksH = (renderHeight.toDouble() / chunkSize.toDouble() + 0.5).roundToInt()

        fitArrayListToSize(rgbaBuffers, chunksW * chunksH) { ByteBuffer.allocate(3 * chunkSize * chunkSize) }

        gl.glReadBuffer(gl.defaultReadBuffer)
        for (i in 0 until chunksW)
            for (r in 0 until chunksH)
                gl.glReadPixels(i * chunkSize, r * chunkSize, chunkSize, chunkSize, GL_RGB, GL_UNSIGNED_BYTE, rgbaBuffers[r * chunksW + i])

        renderingState = RenderState.DRAW_FX
    }

    private fun <T> fitArrayListToSize(list: ArrayList<T>, size: Int, instanceCreator: () -> T){
        while(list.size != size) {
            if(list.size > size)
                list.removeLast()
            if(list.size < size)
                list.add(instanceCreator.invoke())
        }
    }

    private fun updateGLSize(){
        val dpi = scene.window.outputScaleX
        val width = if(width > 0) (width * dpi) else 300.0
        val height = if(height > 0) (height * dpi) else 300.0

        glWindow.setSize(width.toInt(), height.toInt())
    }
}