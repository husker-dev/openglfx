package com.huskerdev.openglfx.internal

import com.huskerdev.grapl.core.platform.BackgroundMessageHandler
import com.huskerdev.grapl.core.x
import com.huskerdev.grapl.gl.GLContext
import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.grapl.gl.GLWindow
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
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
import com.sun.javafx.sg.prism.NGNode
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
    val canvas: GLCanvas,
    val executor: GLExecutor,
    val profile: GLProfile,
    val glDebug: Boolean,
    val externalWindow: Boolean
): NGRegion() {

    companion object {
        fun create(
            canvas: GLCanvas,
            executor: GLExecutor,
            profile: GLProfile,
            glDebug: Boolean,
            interopType: GLInteropType,
            externalWindow: Boolean
        ) = when(interopType){
            GLInteropType.Blit -> ::BlitCanvas
            GLInteropType.WGLDXInterop -> ::WGLDXInteropCanvas
            GLInteropType.ExternalObjectsWinD3D -> ::ExternalObjectsCanvasWinD3D
            GLInteropType.ExternalObjectsWinES -> ::ExternalObjectsCanvasWinES2
            GLInteropType.ExternalObjectsFd -> ::ExternalObjectsCanvasFd
            GLInteropType.IOSurface -> ::IOSurfaceCanvas
        }(canvas, executor, profile, glDebug, externalWindow)
    }

    @Volatile var disposed = false
        private set

    val width by canvas::width
    val height by canvas::height
    val scaledWidth by canvas::scaledWidth
    val scaledHeight by canvas::scaledHeight

    val flipY by canvas::flipY
    val msaa by canvas::msaa
    val fxaa by canvas::fxaa

    var fps = 0

    private val renderLock = Object()
    private lateinit var renderThread: Thread
    private var readyToDisplay = AtomicBoolean(false)
    private var lastFrameStartTime = 0L

    protected lateinit var window: GLWindow // Used when 'externalWindow' is true
    protected lateinit var context: GLContext

    private val swapChain = Array(canvas.swapBuffers) { createSwapBuffer() }
    private var currentSwapBufferIndex = AtomicInteger(-1)

    private val animationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            if(readyToDisplay.getAndSet(false)){
                NodeHelper.markDirty(canvas, DirtyBits.NODE_BOUNDS)
                NodeHelper.markDirty(canvas, DirtyBits.REGION_SHAPE)
            }
        }
    }.apply { start() }

    init {
        Platform.runLater(::requestRepaint)
    }

    protected abstract fun onRenderThreadInit()
    protected abstract fun createSwapBuffer(): SwapBuffer

    fun requestRepaint() = synchronized(renderLock){
        renderLock.notifyAll()
    }

    open fun dispose(){
        animationTimer.stop()
        disposed = true
        swapChain.forEach {
            synchronized(it.lock){
                it.disposeFXResources()
            }
        }
        if(externalWindow){
            window.destroy()
            com.huskerdev.grapl.core.platform.Platform.current.peekMessages()
        }
    }

    private fun createRenderingThread(){
        renderThread = thread(isDaemon = true) {
            if(externalWindow){
                BackgroundMessageHandler.useHandler = false
                window = GLWindow(profile, debug = glDebug).apply {
                    closable = false
                    maximizable = false
                    resizable = false
                    title = "openglfx external window"
                    visible = true
                }
                context = window.context
            }else
                context = GLContext.create(profile = profile, debug = glDebug)
            context.makeCurrent()

            GLContext.bindDebugCallback(::println) // Debug

            executor.initGLFunctions()

            onRenderThreadInit()

            while(!disposed) {
                lastFrameStartTime = System.currentTimeMillis()

                val canvasWidth = max(1, scaledWidth)
                val canvasHeight = max(1, scaledHeight)

                val swapBufferIndex = (currentSwapBufferIndex.get() + 1) % swapChain.size
                val swapBuffer = swapChain[swapBufferIndex]

                synchronized(swapBuffer.lock){
                    glViewport(0, 0, canvasWidth, canvasHeight)
                    val buffer = swapBuffer.render(canvasWidth, canvasHeight)
                    if(externalWindow){
                        if(window.absoluteSize != canvasWidth x canvasHeight)
                            window.absoluteSize = canvasWidth x canvasHeight
                        buffer.blitTo(0)
                        com.huskerdev.grapl.core.platform.Platform.current.peekMessages()
                        window.swapBuffers()
                    }
                }

                currentSwapBufferIndex.set(swapBufferIndex)
                readyToDisplay.set(true)

                synchronized(renderLock) {
                    if(fps > 0) {
                        val delay = (1000 / fps) - (System.currentTimeMillis() - lastFrameStartTime)
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

        val swapElement = swapChain[currentSwapBufferIndex.get()]

        synchronized(swapElement.lock){
            val texture = swapElement.getTextureForDisplay()

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
        abstract fun getTextureForDisplay(): Texture
        abstract fun dispose()
        abstract fun disposeFXResources()

        protected fun createFramebufferForRender(width: Int, height: Int) =
            if(msaa > 0) Framebuffer.MultiSampled(width, height, msaa)
            else         Framebuffer.Default(width, height)
    }

    override fun setParent(parent: NGNode?) {
        super.setParent(parent)
        if(canvas.scene != null)
            canvas.fireSceneBoundEvent()
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