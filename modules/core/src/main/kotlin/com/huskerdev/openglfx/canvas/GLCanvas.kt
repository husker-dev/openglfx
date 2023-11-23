package com.huskerdev.openglfx.canvas

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import com.huskerdev.openglfx.internal.*
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.internal.GLInteropType.*
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import javafx.scene.layout.Pane
import java.util.function.Consumer

enum class GLProfile {
    Core, Compatibility;
}

abstract class GLCanvas(
    val interopType: GLInteropType,
    val profile: GLProfile,
    val flipY: Boolean,
    val msaa: Int,
    val isAsync: Boolean
): Pane() {

    companion object {
        init {
            GLFXUtils.loadLibrary()
            RegionAccessorOverrider.overwrite(object : RegionAccessorObject<GLCanvas>() {
                override fun doCreatePeer(node: GLCanvas) = NGGLCanvas(node, node::onNGRender)
            })
        }

        /**
         * Creates compatible OpenGLCanvas instance with specified GL library and profile
         *
         * @param executor OpenGL implementation library
         *  - LWJGL_MODULE;
         *  - JOGL_MODULE.
         * @param profile Core/Compatibility OpenGL profile
         *  - GLProfile.Compatibility (default).
         *  - GLProfile.Core;
         * @param flipY Flip Y axis
         *  - false – 0 is bottom (default).
         *  - true – 0 is top;
         * @param msaa Multisampling quality
         *  - 0 – disabled (default);
         *  - -1 – maximum available samples.
         * @param async Enables rendering in different thread
         *  - false – render in JavaFX thread (default);
         *  - true – render in a new thread and synchronise with JavaFX.
         * @return OpenGLCanvas instance
         */
        @JvmOverloads
        @JvmStatic
        fun create(
            executor: GLExecutor,
            profile: GLProfile = GLProfile.Compatibility,
            flipY: Boolean = false,
            msaa: Int = 0,
            async: Boolean = false,
            interopType: GLInteropType = GLInteropType.supported
        ) = when (interopType) {
            IOSurface -> executor::ioSurfaceCanvas
            TextureSharing -> executor::sharedCanvas
            NVDXInterop -> executor::interopCanvas
            Blit -> executor::blitCanvas
        }(profile, flipY, msaa, async)
    }

    @Volatile var disposed = false
        private set

    private var onInit = arrayListOf<Consumer<GLInitializeEvent>>()
    private var onRender = arrayListOf<Consumer<GLRenderEvent>>()
    private var onReshape = arrayListOf<Consumer<GLReshapeEvent>>()
    private var onDispose = arrayListOf<Consumer<GLDisposeEvent>>()

    private fun <T> ArrayList<Consumer<T>>.dispatchEvent(event: T) = forEach { it.accept(event) }
    private var initEventsChanged = false

    val fpsCounter = FPSCounter()

    /**
     *  Binds GLCanvasAnimator to the OpenGLCanvas.
     */
    var animator: GLCanvasAnimator? = null
        set(value) {
            field?.unbind()
            value?.bind(this)
            field = value
        }

    val dpi: Double
        get() = scene?.window?.outputScaleX ?: 1.0

    val scaledWidth: Int
        get() = (width * dpi).toInt()

    val scaledHeight: Int
        get() = (height * dpi).toInt()

    private var useRenderDoc = false

    init {
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                timerTick()
            }
        }.start()
    }

    protected abstract fun timerTick()
    protected abstract fun onNGRender(g: Graphics)
    abstract fun repaint()

    protected fun markDirty(){
        NodeHelper.markDirty(this, DirtyBits.NODE_BOUNDS)
        NodeHelper.markDirty(this, DirtyBits.REGION_SHAPE)
    }

    /**
     * Invokes every frame with an active GL context
     *
     * @param listener
     * @return true (specified by [java.util.Collection.add])
     */
    fun addOnRenderEvent(listener: Consumer<GLRenderEvent>) = onRender.add(listener)

    /**
     * Invokes when OpenGLCanvas surface is resized, with GL context
     *
     * @param listener consumer with GLReshapeEvent
     * @return true (specified by [java.util.Collection.add])
     */
    fun addOnReshapeEvent(listener: Consumer<GLReshapeEvent>) = onReshape.add(listener)

    /**
     * Invokes once when OpenGLCanvas is destroyed
     *
     * @param listener consumer with GLDisposeEvent
     * @return true (specified by [java.util.Collection.add])
     */
    fun addOnDisposeEvent(listener: Consumer<GLDisposeEvent>) = onDispose.add(listener)

    /**
     * Invokes once before rendering, with GL context
     *
     * @param listener consumer with GLInitializeEvent
     * @return true (specified by [java.util.Collection.add])
     */
    fun addOnInitEvent(listener: Consumer<GLInitializeEvent>): Boolean {
        initEventsChanged = true
        return onInit.add(listener)
    }

    /**
     *  Destroys all resources to free up memory
     */
    open fun dispose(){
        disposed = true
        animator = null
        onDispose.dispatchEvent(createDisposeEvent())
    }

    /**
     *  These methods must be invoked from OpenGLCanvas implementations
     */
    protected fun fireRenderEvent(fbo: Int) {
        checkInitializationEvents()
        fpsCounter.update()
        onRender.dispatchEvent(createRenderEvent(fpsCounter.currentFps, fpsCounter.delta, scaledWidth, scaledHeight, fbo))
    }

    protected fun fireReshapeEvent(width: Int, height: Int) {
        checkInitializationEvents()
        onReshape.dispatchEvent(createReshapeEvent(width, height))
    }

    protected fun fireInitEvent() = checkInitializationEvents()
    protected fun fireDisposeEvent() = onDispose.dispatchEvent(createDisposeEvent())

    /**
     *  Possibility to override events
     *  (Used by JOGL)
     */
    protected open fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int)
        = GLRenderEvent(GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    protected open fun createReshapeEvent(width: Int, height: Int)
        = GLReshapeEvent(GLReshapeEvent.ANY, width, height)
    protected open fun createInitEvent()
        = GLInitializeEvent(GLInitializeEvent.ANY)
    protected open fun createDisposeEvent()
        = GLDisposeEvent(GLDisposeEvent.ANY)

    /**
     * Checks if there are any not initialised listeners
     */
    private fun checkInitializationEvents(){
        while(onInit.size > 0)
            onInit.removeLast().accept(createInitEvent())
    }

    /**
     * Fills node by texture
     *
     * @param g Node's graphics
     * @param texture default JavaFX texture
     */
    protected fun drawResultTexture(g: Graphics, texture: Texture){
        if(flipY) g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        else      g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, scaledHeight.toFloat(), scaledWidth.toFloat(), 0f)
    }

}