package com.huskerdev.openglfx

import com.huskerdev.openglfx.events.GLDisposeEvent
import com.huskerdev.openglfx.events.GLInitializeEvent
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import com.huskerdev.openglfx.utils.FpsCounter
import com.huskerdev.openglfx.utils.OpenGLFXLibLoader
import com.huskerdev.openglfx.utils.RegionAccessorObject
import com.huskerdev.openglfx.utils.RegionAccessorOverrider
import com.huskerdev.openglfx.utils.windows.DXInterop
import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.Texture
import javafx.scene.layout.Pane
import java.util.function.Consumer

enum class GLProfile {
    Core, Compatibility;
}

abstract class OpenGLCanvas(
    val profile: GLProfile
): Pane() {

    companion object {
        init {
            OpenGLFXLibLoader.load()
            RegionAccessorOverrider.overwrite(object : RegionAccessorObject<OpenGLCanvas>() {
                override fun doCreatePeer(node: OpenGLCanvas) = NGOpenGLCanvas(node)
            })
        }

        /**
         * Creates compatible OpenGLCanvas instance with specified GL library and profile
         *
         * @param executor OpenGL implementation library
         *  - LWJGL_MODULE
         *  - JOGL_MODULE
         * @param profile Core/Compatibility OpenGL profile
         *  - GLProfile.Core
         *  - GLProfile.Compatibility
         * @return OpenGLCanvas instance
         */
        @JvmOverloads
        @JvmStatic
        fun create(executor: GLExecutor, profile: GLProfile = GLProfile.Compatibility) =
             when (GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]) {
                "es2" -> executor::sharedCanvas
                "d3d" -> if (DXInterop.isSupported()) executor::interopCanvas else executor::universalCanvas
                else -> executor::universalCanvas
            }(profile)
    }

    private var onInit = arrayListOf<InitListenerContainer>()
    private var onRender = arrayListOf<Consumer<GLRenderEvent>>()
    private var onReshape = arrayListOf<Consumer<GLReshapeEvent>>()
    private var onDispose = arrayListOf<Consumer<GLDisposeEvent>>()

    private var initEventsChanged = false
    private fun <T> ArrayList<Consumer<T>>.dispatchEvent(event: T) = forEach { it.accept(event) }

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

    protected abstract fun onNGRender(g: Graphics)
    abstract fun repaint()

    /**
     * Invokes every frame with an active GL context
     *
     * @param listener
     * @return true (as specified by Collection.add)
     */
    fun addOnRenderEvent(listener: Consumer<GLRenderEvent>) = onRender.add(listener)

    /**
     * Invokes when OpenGLCanvas surface is resized, with GL context
     *
     * @param listener consumer with GLReshapeEvent
     * @return true (as specified by Collection.add)
     */
    fun addOnReshapeEvent(listener: Consumer<GLReshapeEvent>) = onReshape.add(listener)

    /**
     * Invokes once when OpenGLCanvas is destroyed
     *
     * @param listener consumer with GLDisposeEvent
     * @return true (as specified by Collection.add)
     */
    fun addOnDisposeEvent(listener: Consumer<GLDisposeEvent>) = onDispose.add(listener)

    /**
     * Invokes once before rendering, with GL context
     *
     * @param listener consumer with GLInitializeEvent
     * @return true (as specified by Collection.add)
     */
    fun addOnInitEvent(listener: Consumer<GLInitializeEvent>): Boolean {
        initEventsChanged = true
        return onInit.add(InitListenerContainer(listener))
    }

    /**
     *  These methods must be invoked from OpenGLCanvas implementations
     */
    protected fun fireRenderEvent() {
        checkInitialization()
        fpsCounter.update()
        onRender.dispatchEvent(createRenderEvent(fpsCounter.currentFps, fpsCounter.delta, scaledWidth.toInt(), scaledHeight.toInt()))
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
    protected open fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int)
        = GLRenderEvent(GLRenderEvent.ANY, currentFps, delta, width, height)
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
        g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, scaledHeight.toFloat(), scaledWidth.toFloat(), 0f)
    }

    private class NGOpenGLCanvas(val canvas: OpenGLCanvas): NGRegion() {
        override fun renderContent(g: Graphics) {
            canvas.onNGRender(g)
            super.renderContent(g)
        }
    }

    private class InitListenerContainer(
        val event: Consumer<GLInitializeEvent>,
        var initialized: Boolean = false
    )
}