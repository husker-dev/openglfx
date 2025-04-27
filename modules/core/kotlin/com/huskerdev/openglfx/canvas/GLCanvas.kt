package com.huskerdev.openglfx.canvas

import com.huskerdev.grapl.core.platform.BackgroundMessageHandler
import com.huskerdev.grapl.gl.GLContext
import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.grapl.gl.GLWindow
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import com.huskerdev.openglfx.internal.*
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.dispatchConsumer
import com.sun.javafx.scene.layout.RegionHelper
import javafx.scene.Node
import javafx.scene.layout.Region
import java.util.function.Consumer
import kotlin.math.ceil

/**
 * Hardware-accelerated OpenGL canvas.
 *
 * @param executor OpenGL implementation library:
 *  - LWJGL_MODULE;
 *  - LWJGL2_MODULE (legacy);
 *  - JOGL_MODULE;
 *  - LIBGDX_MODULE (beta);
 *  - NONE_MODULE.
 * @param flipY Flip Y axis:
 *  - false – 0 is bottom (default);
 *  - true – 0 is top.
 * @param msaa Multisampling anti-aliasing quality:
 *  - 0 – disabled (default);
 *  - -1 – maximum available samples.
 *  @param fps Frames per seconds:
 *  - < 0 - Monitor refresh rate
 *  - 0	- Do not update repeatedly
 *  - \> 0 Update with desired FPS
 *  @param swapBuffers Swap-chain buffers:
 *  - The best UI performance is achieved with 2 (default).
 *  - The most responsive to resizing is 1.
 *  @param interopType Type of interop between JavaFX and OpenGL (do not change if you not sure what you do)
 */
open class GLCanvas @JvmOverloads protected constructor(
    val executor: GLExecutor,
    val flipY: Boolean              = false,
    val msaa: Int                   = 0,
    fps: Double                     = -1.0,
    val swapBuffers: Int            = 2,
    val interopType: GLInteropType  = GLInteropType.auto,
): Region() {

    companion object {
        init {
            GLFXUtils.loadLibrary()
        }
    }

    var context: GLContext? = null
    var window: GLWindow? = null

    private var onInit = arrayListOf<Consumer<GLInitializeEvent>>()
    private var onRender = arrayListOf<Consumer<GLRenderEvent>>()
    private var onReshape = arrayListOf<Consumer<GLReshapeEvent>>()
    private var onDispose = arrayListOf<Consumer<GLDisposeEvent>>()

    /**
     * Store current FPS, delta time and frame id.
     */
    val fpsCounter = FPSCounter()

    /**
     * A current window dpi, used to scale output width and height.
     */
    val dpi: Double
        get() = GLFXUtils.getDPI(this)

    /**
     * Current node width, considering DPI scaling.
     */
    val scaledWidth: Int
        get() = ceil(width * dpi).toInt()

    /**
     * Current node height, considering DPI scaling.
     */
    val scaledHeight: Int
        get() = ceil(height * dpi).toInt()

    /**
     * Frames per seconds:
     *  - < 0 - Monitor refresh rate
     *  - 0	- Do not update repeatedly
     *  - \> 0 Update with desired FPS
     */
    var fps: Double = 0.0
        set(value) {
            field = value
            RegionHelper.getPeer<NGGLCanvas>(this).fps =
                if(value >= 0) value else -1.0
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
     *  @param fps Frames per seconds:
     *  - < 0 - Monitor refresh rate
     *  - 0	- Do not update repeatedly
     *  - \> 0 Update with desired FPS
     *  @param glDebug Creates GLContext that supports debugging
     *  @param swapBuffers Swap-chain buffers:
     *  - The best UI performance is achieved with 2 (default).
     *  - The most responsive to resizing is 1.
     *  @param interopType Type of interop between JavaFX and OpenGL (do not change if you not sure what you do)
     *  @param externalWindow Creates external window that mirroring canvas. Used to enable debugging via NSight or RenderDoc.
     *  @param shareWith
     */
    constructor(
        executor: GLExecutor,
        profile: GLProfile          = GLProfile.CORE,
        flipY: Boolean              = false,
        msaa: Int                   = 0,
        fps: Double                 = -1.0,
        glDebug: Boolean            = false,
        swapBuffers: Int            = 2,
        interopType: GLInteropType  = GLInteropType.auto,
        externalWindow: Boolean     = false,
        shareWith: GLContext?       = null,
        majorVersion: Int           = -1,
        minorVersion: Int           = -1,
    ): this(
        executor, flipY, msaa,
        fps, swapBuffers, interopType
    ) {
        if(externalWindow){
            BackgroundMessageHandler.useHandler = false
            window = GLWindow(profile, debug = glDebug).apply {
                closable = false
                maximizable = false
                resizable = false
                title = "openglfx external window"
                visible = true
            }
            context = window!!.context
        }else
            context = GLContext.create(shareWith?.handle ?: 0, profile, majorVersion, minorVersion, glDebug)
    }

    @JvmOverloads protected constructor(
        executor: GLExecutor,
        flipY: Boolean              = false,
        msaa: Int                   = 0,
        fps: Double                 = -1.0,
        swapBuffers: Int            = 2,
        interopType: GLInteropType  = GLInteropType.auto,
        context: GLContext
    ): this(executor, flipY, msaa, fps, swapBuffers, interopType) {
        this.context = context
    }

    @JvmOverloads protected constructor(
        executor: GLExecutor,
        flipY: Boolean              = false,
        msaa: Int                   = 0,
        fps: Double                 = -1.0,
        swapBuffers: Int            = 2,
        interopType: GLInteropType  = GLInteropType.auto,
        window: GLWindow
    ): this(executor, flipY, msaa, fps, swapBuffers, interopType) {
        this.window = window
        this.context = window.context
    }

    init {
        object: RegionHelper(){
            init {
                setHelper(this@GLCanvas, this)
            }
            override fun createPeerImpl(node: Node) =
                if(node == this@GLCanvas) doCreatePeer()
                else super.createPeerImpl(node)
        }
        this.fps = fps
    }

    /*===========================================*\
    |                 Listeners                   |
    \*===========================================*/

    /**
     * Invokes every frame with an active GL context
     *
     * @param listener
     * @return true (specified by [java.util.Collection.add])
     */
    @Suppress("unused")
    fun addOnRenderEvent(listener: Consumer<GLRenderEvent>) = onRender.add(listener)

    /**
     * Invokes when OpenGLCanvas surface is resized, with GL context
     *
     * @param listener consumer with GLReshapeEvent
     * @return true (specified by [java.util.Collection.add])
     */
    @Suppress("unused")
    fun addOnReshapeEvent(listener: Consumer<GLReshapeEvent>) = onReshape.add(listener)

    /**
     * Invokes once when OpenGLCanvas is destroyed
     *
     * @param listener consumer with GLDisposeEvent
     * @return true (specified by [java.util.Collection.add])
     */
    @Suppress("unused")
    fun addOnDisposeEvent(listener: Consumer<GLDisposeEvent>) = onDispose.add(listener)

    /**
     * Invokes once before rendering, with GL context
     *
     * @param listener consumer with GLInitializeEvent
     * @return true (specified by [java.util.Collection.add])
     */
    @Suppress("unused")
    fun addOnInitEvent(listener: Consumer<GLInitializeEvent>) = onInit.add(listener)


    /*===========================================*\
    |                Events firing                |
    \*===========================================*/

    /**
     *  Invokes every rendering listener.
     *
     *  @param fbo result framebuffer that is bound to Node
     */
    open fun fireRenderEvent(fbo: Int) {
        fireInitEvent()
        fpsCounter.update()
        onRender.dispatchConsumer(executor.createRenderEvent(
            this,
            fpsCounter.currentFps,
            fpsCounter.delta,
            scaledWidth, scaledHeight, fbo))
    }

    /**
     *  Invokes every resizing listener.
     *
     *  @param width new framebuffer width
     *  @param height new framebuffer height
     */
    open fun fireReshapeEvent(width: Int, height: Int) {
        fireInitEvent()
        onReshape.dispatchConsumer(executor.createReshapeEvent(this, width, height))
    }

    /**
     *  Invokes every initialization listener.
     */
    open fun fireInitEvent() {
        while(onInit.size > 0)
            onInit.removeAt(onInit.lastIndex).accept(executor.createInitEvent(this))
    }

    /**
     *  Invokes every disposing listener.
     */
    open fun fireDisposeEvent() = onDispose.dispatchConsumer(executor.createDisposeEvent(this))


    /*===========================================*\
    |                     Peer                    |
    \*===========================================*/
    private fun doCreatePeer() =
        NGGLCanvas.create(this, interopType)

    /**
     *  Destroys all resources to free up memory
     */
    fun dispose() =
        RegionHelper.getPeer<NGGLCanvas>(this).dispose()

    fun repaint() =
        RegionHelper.getPeer<NGGLCanvas>(this).requestRepaint()
}