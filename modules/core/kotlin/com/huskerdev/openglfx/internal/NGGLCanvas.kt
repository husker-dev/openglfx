package com.huskerdev.openglfx.internal

import com.huskerdev.grapl.core.x
import com.huskerdev.openglfx.GLExecutor.Companion.glGetInteger
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.GL_MAX_SAMPLES
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.canvas.BlitCanvas
import com.huskerdev.openglfx.internal.canvas.ExternalObjectsCanvasWinD3D
import com.huskerdev.openglfx.internal.canvas.ExternalObjectsCanvasWinES2
import com.huskerdev.openglfx.internal.canvas.IOSurfaceCanvas
import com.huskerdev.openglfx.internal.canvas.ExternalObjectsCanvasFd
import com.huskerdev.openglfx.internal.canvas.WGLDXInteropCanvas
import com.sun.javafx.geom.BaseBounds
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import javafx.application.Platform
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.math.max

abstract class NGGLCanvas(
    val canvas: GLCanvas
): NGRegion() {

    companion object {
        fun create(
            canvas: GLCanvas,
            interopType: GLInteropType
        ) = when(interopType){
            GLInteropType.Blit                  -> ::BlitCanvas
            GLInteropType.WGLDXInterop          -> ::WGLDXInteropCanvas
            GLInteropType.ExternalObjectsWinD3D -> ::ExternalObjectsCanvasWinD3D
            GLInteropType.ExternalObjectsWinES  -> ::ExternalObjectsCanvasWinES2
            GLInteropType.ExternalObjectsFd     -> ::ExternalObjectsCanvasFd
            GLInteropType.IOSurface             -> ::IOSurfaceCanvas
        }(canvas)
    }

    @Volatile
    private var disposed = false

    private var hasScene = false

    protected val width by canvas::width
    protected val height by canvas::height
    private val scaledWidth by canvas::scaledWidth
    private val scaledHeight by canvas::scaledHeight

    private val flipY by canvas::flipY
    protected val msaa by canvas::msaa
    private var fps by canvas::fps

    private val renderLock = Object()
    private lateinit var renderThread: Thread
    private var readyToDisplay = AtomicBoolean(false)
    private var lastFrameStartTime = 0L

    private val executor = canvas.executor
    private val context = canvas.context
    private var window = canvas.window
    private val useExternalWindow = window != null

    private val swapChain = Array(canvas.swapBuffers) { createSwapBuffer() }
    private var currentSwapBufferIndex = AtomicInteger(-1)

    private val animationTimer = object: AnimationTimer() {
        override fun handle(now: Long) {
            if(!canPaint())
                return
            if(readyToDisplay.getAndSet(false)){
                NodeHelper.markDirty(canvas, DirtyBits.NODE_BOUNDS)
                NodeHelper.markDirty(canvas, DirtyBits.REGION_SHAPE)
            }
            if(fps < 0)
                requestRepaint()
        }
    }.apply { start() }

    init {
        Platform.runLater(::requestRepaint)

        canvas.sceneProperty().addListener { _ ->
            hasScene = canvas.scene != null
            if(hasScene)
                requestRepaint()
        }
        hasScene = canvas.scene != null
    }

    protected abstract fun onRenderThreadInit()
    protected abstract fun createSwapBuffer(): SwapBuffer

    private fun canPaint() =
        isVisible && hasScene

    fun requestRepaint() {
        if(!canPaint())
            return
        synchronized(renderLock){
            renderLock.notifyAll()
        }
    }

    open fun dispose(){
        animationTimer.stop()
        disposed = true
        swapChain.forEach {
            synchronized(it.lock){
                it.disposeFXResources()
            }
        }
        if(useExternalWindow){
            window!!.destroy()
            com.huskerdev.grapl.core.platform.Platform.current.peekMessages()
        }
    }

    private fun createRenderingThread(){
        renderThread = thread(isDaemon = true) {

            context.makeCurrent()
            executor.initGLFunctions()
            onRenderThreadInit()

            while(!disposed) {
                lastFrameStartTime = System.currentTimeMillis()

                val canvasWidth = max(1, scaledWidth)
                val canvasHeight = max(1, scaledHeight)

                val swapBufferIndex = (currentSwapBufferIndex.get() + 1) % swapChain.size
                val swapBuffer = swapChain[swapBufferIndex]

                synchronized(swapBuffer.lock){
                    if(disposed)
                        return@synchronized

                    glViewport(0, 0, canvasWidth, canvasHeight)
                    val buffer = swapBuffer.render(canvasWidth, canvasHeight)

                    if(useExternalWindow){
                        if(window!!.absoluteSize != canvasWidth x canvasHeight)
                            window!!.absoluteSize = canvasWidth x canvasHeight
                        buffer.blitTo(0)
                        com.huskerdev.grapl.core.platform.Platform.current.peekMessages()
                        window!!.swapBuffers()
                    }
                }

                currentSwapBufferIndex.set(swapBufferIndex)
                readyToDisplay.set(true)

                synchronized(renderLock) {
                    if(canPaint() && fps > 0) {
                        val delay = ((1000 / fps) - (System.currentTimeMillis() - lastFrameStartTime)).toLong()
                        if(delay > 0)
                            renderLock.wait(delay)
                    }else
                        renderLock.wait()
                }
            }

            swapChain.forEach { it.dispose() }
            context.delete()
        }
    }

    override fun renderContent(g: Graphics) {
        if(!this::renderThread.isInitialized)
            createRenderingThread()

        val swapElementIndex = currentSwapBufferIndex.get()
        if(swapElementIndex == -1)
            return

        val swapElement = swapChain[swapElementIndex]

        synchronized(swapElement.lock){
            val texture = swapElement.getTextureForDisplay(g)

            val drawWidth = (texture.physicalWidth / canvas.dpi).toFloat()
            val drawHeight = (texture.physicalHeight / canvas.dpi).toFloat()
            val sourceWidth = texture.physicalWidth.toFloat()
            val sourceHeight = texture.physicalHeight.toFloat()

            if(flipY) g.drawTexture(texture, 0f, 0f, drawWidth, drawHeight, 0f, 0f, sourceWidth, sourceHeight)
            else      g.drawTexture(texture, 0f, 0f, drawWidth, drawHeight, 0f, sourceHeight, sourceWidth, 0f)
        }
    }

    protected abstract inner class SwapBuffer {
        val lock = Object()

        abstract fun render(width: Int, height: Int): Framebuffer
        abstract fun getTextureForDisplay(g: Graphics): Texture
        abstract fun dispose()
        abstract fun disposeFXResources()

        protected fun createFramebufferForRender(width: Int, height: Int) = when {
            msaa > 0 -> Framebuffer.MultiSampled(width, height, msaa)
            msaa < 0 -> Framebuffer.MultiSampled(width, height, glGetInteger(GL_MAX_SAMPLES))
            else ->     Framebuffer.Default(width, height)
        }
    }

    override fun setTransformedBounds(bounds: BaseBounds?, byTransformChangeOnly: Boolean) {
        super.setTransformedBounds(bounds, byTransformChangeOnly)
        requestRepaint()
    }

    override fun setVisible(value: Boolean) {
        super.setVisible(value)
        requestRepaint()
    }
}