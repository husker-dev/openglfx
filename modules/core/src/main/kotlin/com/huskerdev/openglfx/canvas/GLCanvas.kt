package com.huskerdev.openglfx.canvas

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import com.huskerdev.openglfx.internal.*
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.dispatchEvent
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.dispatchJavaEvent
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.internal.GLInteropType.*
import com.sun.javafx.scene.layout.RegionHelper
import javafx.scene.Scene
import javafx.scene.layout.Pane
import java.util.function.Consumer

enum class GLProfile {
    Core, Compatibility;
}

/**
 * Hardware-accelerated OpenGL canvas.
 *
 * @param executor OpenGL implementation library:
 *  - LWJGL_MODULE;
 *  - LWJGL2_MODULE (legacy);
 *  - JOGL_MODULE;
 *  - LIBGDX_MODULE (beta);
 *  - NONE_MODULE.
 * @param profile Core/Compatibility OpenGL profile:
 *  - GLProfile.Compatibility (default);
 *  - GLProfile.Core.
 * @param flipY Flip Y axis:
 *  - false – 0 is bottom (default);
 *  - true – 0 is top.
 * @param msaa Multisampling anti-aliasing quality:
 *  - 0 – disabled (default);
 *  - -1 – maximum available samples.
 * @param fxaa Fast approximate anti-aliasing:
 *  - false – disabled (default);
 *  - true – enabled.
 * @param async Enables rendering in parallel thread:
 *  - false – render in JavaFX thread (default);
 *  - true – render in a new thread and synchronise with JavaFX.
 */
open class GLCanvas @JvmOverloads constructor(
    val executor: GLExecutor,
    val profile: GLProfile          = GLProfile.Compatibility,
    val flipY: Boolean              = false,
    val msaa: Int                   = 0,
    val fxaa: Boolean               = false,
    val async: Boolean              = false,
    val interopType: GLInteropType  = GLInteropType.supported
): Pane() {

    companion object {
        init {
            GLFXUtils.loadLibrary()
            RegionAccessorOverrider.overwrite(object : RegionAccessorObject<GLCanvas>() {
                override fun doCreatePeer(node: GLCanvas) = node.doCreatePeer()
            })
        }
    }

    private var onInit = arrayListOf<Consumer<GLInitializeEvent>>()
    private var onRender = arrayListOf<Consumer<GLRenderEvent>>()
    private var onReshape = arrayListOf<Consumer<GLReshapeEvent>>()
    private var onDispose = arrayListOf<Consumer<GLDisposeEvent>>()

    private val onRenderBegin = arrayListOf<() -> Unit>()
    private val onRenderEnd = arrayListOf<() -> Unit>()
    private val onSceneBound = arrayListOf<(Scene) -> Unit>()

    /**
     * Store current FPS, delta time and frame id.
     */
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

    /**
     * A current window dpi, used to scale output width and height.
     */
    val dpi: Double
        get() = GLFXUtils.getDPI(this)

    /**
     * Current node width, considering DPI scaling.
     */
    val scaledWidth: Int
        get() = (width * dpi).toInt()

    /**
     * Current node height, considering DPI scaling.
     */
    val scaledHeight: Int
        get() = (height * dpi).toInt()

    /*===========================================*\
    |                 Listeners                   |
    \*===========================================*/

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
    fun addOnInitEvent(listener: Consumer<GLInitializeEvent>) = onInit.add(listener)

    /**
     * Internal method. Invokes BEFORE rendering frame.
     *
     * @param listener
     */
    internal fun addOnRenderBegin(listener: () -> Unit) = onRenderBegin.add(listener)

    /**
     * Internal method. Invokes AFTER rendering frame.
     *
     * @param listener
     */
    internal fun addOnRenderEnd(listener: () -> Unit) = onRenderEnd.add(listener)

    /**
     * Internal method for listening bound scene.
     * If the scene is already connected, then listener invokes immediately.
     *
     * @param listener invokes when scene is connected to the node
     */
    internal fun addOnSceneConnected(listener: (Scene) -> Unit){
        if(scene != null) listener(scene)
        else onSceneBound.add(listener)
    }

    /*===========================================*\
    |                Events firing                |
    \*===========================================*/

    /**
     *  Invokes every rendering listener.
     *
     *  @param fbo result framebuffer that is bound to Node
     */
    fun fireRenderEvent(fbo: Int) {
        fireInitEvent()
        fpsCounter.update()
        onRenderBegin.dispatchEvent()
        onRender.dispatchJavaEvent(executor.createRenderEvent(
            this,
            fpsCounter.currentFps,
            fpsCounter.delta,
            scaledWidth, scaledHeight, fbo))
        onRenderEnd.dispatchEvent()
    }

    /**
     *  Invokes every resizing listener.
     *
     *  @param width new framebuffer width
     *  @param height new framebuffer height
     */
    fun fireReshapeEvent(width: Int, height: Int) {
        fireInitEvent()
        onReshape.dispatchJavaEvent(executor.createReshapeEvent(this, width, height))
    }

    /**
     *  Invokes every initialization listener.
     */
    fun fireInitEvent() {
        while(onInit.size > 0)
            onInit.removeLast().accept(executor.createInitEvent(this))
    }

    /**
     *  Invokes every disposing listener.
     */
    fun fireDisposeEvent() = onDispose.dispatchJavaEvent(executor.createDisposeEvent(this))

    /**
     *  Internal method. Invokes every scene binding listener.
     */
    internal fun fireSceneBoundEvent() = onSceneBound.dispatchEvent(scene)

    /*===========================================*\
    |                     Peer                    |
    \*===========================================*/

    internal fun doCreatePeer() =
        when (interopType) {
            IOSurface -> executor::ioSurfaceNGCanvas
            TextureSharing -> executor::sharedNGCanvas
            NVDXInterop -> executor::interopNGCanvas
            Blit -> executor::blitNGCanvas
        }(this, executor, profile, flipY, msaa, async)

    /**
     *  Destroys all resources to free up memory
     */
    fun dispose() {
        animator = null
        RegionHelper.getPeer<NGGLCanvas>(this).dispose()
    }

    fun repaint() = RegionHelper.getPeer<NGGLCanvas>(this).repaint()
}