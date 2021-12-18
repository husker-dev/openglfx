package com.husker.openglfx.jogl.universal

import com.husker.openglfx.jogl.JOGLFXCanvas
import com.husker.openglfx.utils.FXUtils
import com.husker.openglfx.utils.LifetimeLoopThread
import com.jogamp.opengl.*
import com.jogamp.opengl.GL2GL3.*
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.tk.PlatformImage
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Graphics
import com.sun.prism.Image
import com.sun.prism.Texture
import com.sun.prism.impl.BufferUtil
import javafx.animation.AnimationTimer
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import jogamp.opengl.GLDrawableFactoryImpl
import jogamp.opengl.GLOffscreenAutoDrawableImpl
import jogamp.opengl.util.glsl.GLSLTextureRaster
import java.nio.IntBuffer
import kotlin.math.max

class JOGLUniversal(
    capabilities: GLCapabilities
): JOGLFXCanvas() {

    private var drawableFactory: GLDrawableFactoryImpl = GLDrawableFactoryImpl.getFactoryImpl(capabilities.glProfile)
    private lateinit var glWindow: GLOffscreenAutoDrawableImpl
    private lateinit var glslTextureRaster: GLSLTextureRaster

    private val resizeUpdating = LifetimeLoopThread(100){ updateGLSize() }
    private var bufferUpdateRequired = false

    private var image = WritableImage(1, 1)
    private var lastImage = image
    private var imageReady = false

    private lateinit var pixelIntBuffer: IntBuffer
    private lateinit var pixelBuffer: PixelBuffer<IntBuffer>

    init{
        widthProperty().addListener{_, _, _ -> resizeUpdating.startRequest() }
        heightProperty().addListener{_, _, _ -> resizeUpdating.startRequest() }

        Thread{
            capabilities.isFBO = true
            glWindow = drawableFactory.createOffscreenAutoDrawable(GLProfile.getDefaultDevice(), capabilities, null, 10, 10) as GLOffscreenAutoDrawableImpl
            glWindow.addGLEventListener(object: GLEventListener{
                override fun init(drawable: GLAutoDrawable) {
                    renderThread = Thread.currentThread()
                    val gl = drawable.gl
                    glslTextureRaster = GLSLTextureRaster(0, true)
                    glslTextureRaster.init(gl.gL2ES2)
                    glslTextureRaster.reshape(gl.gL2ES2, 0, 0, width.toInt(), height.toInt())
                    fireInitEvent(gl)
                }
                override fun dispose(drawable: GLAutoDrawable) {
                    fireDisposeEvent(drawable.gl)
                }
                override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
                    fireReshapeEvent(drawable.gl)
                }
                override fun display(drawable: GLAutoDrawable) {
                    fireRenderEvent(drawable.gl)
                    readGLPixels(drawable, drawable.gl as GL2)
                    NodeHelper.markDirty(this@JOGLUniversal, DirtyBits.NODE_GEOMETRY)
                }
            })

            FXUtils.onWindowReady(this){ onWindowReady() }
        }.start()

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
    }

    private fun onWindowReady(){
        val oldOnCloseRequest = scene.window.onCloseRequest
        scene.window.setOnCloseRequest {
            glWindow.destroy()
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
            pixelIntBuffer = BufferUtil.newIntBuffer(renderWidth * renderHeight)
            pixelBuffer = PixelBuffer(renderWidth, renderHeight, pixelIntBuffer, PixelFormat.getIntArgbPreInstance())

            image = WritableImage(pixelBuffer)
            imageReady = false
        }

        gl.glReadBuffer(gl.defaultReadBuffer)
        gl.glReadPixels(0, 0, renderWidth, renderHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pixelIntBuffer)
        imageReady = true
        bufferUpdateRequired = true
    }

    private fun updateGLSize() = glWindow.windowResizedOp(max(width * dpi, 1.0).toInt(), max(height * dpi, 1.0).toInt())

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
        glWindow.display()
    }

    private fun WritableImage.getPlatformImage() = Toolkit.getImageAccessor().getPlatformImage(this) as PlatformImage
}