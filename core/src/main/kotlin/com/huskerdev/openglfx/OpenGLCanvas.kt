package com.huskerdev.openglfx

import com.huskerdev.openglfx.events.GLDisposeEvent
import com.huskerdev.openglfx.events.GLInitializeEvent
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import com.huskerdev.openglfx.renderdoc.RenderDoc
import com.huskerdev.openglfx.utils.*
import com.huskerdev.openglfx.utils.windows.DXInterop
import com.sun.javafx.sg.prism.NGNode
import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.Texture
import javafx.scene.input.KeyCode
import javafx.scene.layout.Pane
import java.util.function.Consumer

enum class GLProfile {
    Core, Compatibility;
}

abstract class OpenGLCanvas(
    val profile: GLProfile,
    val flipY: Boolean,
    val msaa: Int,
    val multiThread: Boolean
): Pane() {

    companion object {
        init {
            OGLFXUtils.loadLibrary()
            RegionAccessorOverrider.overwrite(object : RegionAccessorObject<OpenGLCanvas>() {
                override fun doCreatePeer(node: OpenGLCanvas) = NGOpenGLCanvas(node)
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
         * @param multiThread Enables rendering in different thread
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
            multiThread: Boolean = false
        ) = when (GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]) {
            "es2" -> executor::sharedCanvas
            "d3d" -> if (DXInterop.isSupported()) executor::interopCanvas else executor::universalCanvas
            else -> executor::universalCanvas
        }(profile, flipY, msaa, multiThread)
    }

    protected var disposed = false
        private set

    private var onInit = arrayListOf<InitListenerContainer>()
    private var onRender = arrayListOf<Consumer<GLRenderEvent>>()
    private var onReshape = arrayListOf<Consumer<GLReshapeEvent>>()
    private var onDispose = arrayListOf<Consumer<GLDisposeEvent>>()

    private fun <T> ArrayList<Consumer<T>>.dispatchEvent(event: T) = forEach { it.accept(event) }
    private var initEventsChanged = false

    private val fpsCounter = FpsCounter()

    /**
     *  Binds GLCanvasAnimator to the OpenGLCanvas.
     */
    var animator: OpenGLCanvasAnimator? = null
        set(value) {
            field?.unbind()
            value?.bind(this)
            field = value
        }

    private val dpi: Double
        get() = scene?.window?.outputScaleX ?: 1.0

    protected val scaledWidth: Double
        get() = width * dpi

    protected val scaledHeight: Double
        get() = height * dpi

    private var useRenderDoc = false

    protected abstract fun onNGRender(g: Graphics)
    abstract fun repaint()

    private fun onSceneBound(){
        if(RenderDoc.enabled){
            RenderDoc.loadLibrary()
            this.scene.setOnKeyReleased {
                if(it.code == KeyCode.F12){
                    useRenderDoc = true
                    repaint()
                }
            }
        }
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
        return onInit.add(InitListenerContainer(listener))
    }

    /**
     *  Destroys all resources to free up memory
     */
    open fun dispose(){
        animator = null
        disposed = true
    }

    /**
     *  These methods must be invoked from OpenGLCanvas implementations
     */
    protected fun fireRenderEvent(fbo: Int) {
        checkInitialization()
        fpsCounter.update()
        if(useRenderDoc)
            RenderDoc.startFrameCapture()
        onRender.dispatchEvent(createRenderEvent(fpsCounter.currentFps, fpsCounter.delta, scaledWidth.toInt(), scaledHeight.toInt(), fbo))
        if(useRenderDoc){
            RenderDoc.endFrameCapture()
            useRenderDoc = false
        }
    }

    protected fun fireReshapeEvent(width: Int, height: Int) {
        checkInitialization()
        onReshape.dispatchEvent(createReshapeEvent(width, height))
    }

    protected fun fireInitEvent() = checkInitialization()
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
    private fun checkInitialization(){
        if(initEventsChanged){
            initEventsChanged = false
            val event = createInitEvent()

            onInit.forEach {
                if(!it.initialized){
                    it.initialized = true
                    it.event.accept(event)
                }
            }
        }
    }

    /**
     * Fills node by texture
     *
     * @param g Node's graphics
     * @param texture default JavaFX texture
     */
    protected fun drawResultTexture(g: Graphics, texture: Texture){
        if(flipY)
            g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        else
            g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, scaledHeight.toFloat(), scaledWidth.toFloat(), 0f)
    }

    private class NGOpenGLCanvas(val canvas: OpenGLCanvas): NGRegion() {
        private var bound = false

        override fun renderContent(g: Graphics) {
            canvas.onNGRender(g)
            super.renderContent(g)
        }

        override fun setParent(parent: NGNode?) {
            super.setParent(parent)
            if(canvas.scene != null && !bound){
                bound = true
                canvas.onSceneBound()
            }
        }
    }

    private class InitListenerContainer(
        val event: Consumer<GLInitializeEvent>,
        var initialized: Boolean = false
    )
}