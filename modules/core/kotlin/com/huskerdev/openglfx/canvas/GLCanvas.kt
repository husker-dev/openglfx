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
 */
open class GLCanvas private constructor(

    /**
     * OpenGL implementation library
     */
    val executor: GLExecutor,

    /**
     * Flip Y axis:
     * - false – 0 is bottom (default);
     * - true – 0 is top.
     */
    val flipY: Boolean,

    /**
     * Multisampling anti-aliasing quality:
     * - 0 – disabled (default);
     * - -1 – maximum available samples.
     */
    val msaa: Int,

    /**
     * Frames per seconds:
     *  - < 0 - Monitor refresh rate
     *  - 0	- Do not update repeatedly
     *  - \> 0 Update with desired FPS
     */
    var fps: Double,

    /**
     * Swap-chain buffers:
     * - The best UI performance is achieved with 2 (default).
     * - The most responsive to resizing is 1.
     */
    val swapBuffers: Int,

    /**
     * Type of interop between JavaFX and OpenGL
     */
    val interopType: GLInteropType,

    context: Pair<GLContext, GLWindow?>,
): Region() {

    companion object {
        init {
            GLFXUtils.loadLibrary()
        }

        private fun createContextPair(
            profile: GLProfile,
            glDebug: Boolean,
            shareWith: GLContext?,
            majorVersion: Int,
            minorVersion: Int,
            externalWindow: Boolean,
        ) = if(externalWindow){
            BackgroundMessageHandler.useHandler = false
            val window = GLWindow(
                profile = profile,
                shareWith = shareWith?.handle ?: 0,
                majorVersion = majorVersion,
                minorVersion = minorVersion,
                debug = glDebug
            ).apply {
                closable = false
                maximizable = false
                resizable = false
                title = "openglfx external window"
                visible = true
            }
            window.context to window
        }else
            GLContext.create(
                shareWith = shareWith?.handle ?: 0,
                profile = profile,
                majorVersion = majorVersion,
                minorVersion = minorVersion,
                debug = glDebug
            ) to null
    }

    object Defaults {
        const val FLIP_Y = false
        const val MSAA = 0
        const val FPS = -1.0
        const val SWAP_BUFFERS = 2
        val INTEROP_TYPE = GLInteropType.auto

        val PROFILE = GLProfile.CORE
        const val DEBUG = false
        val SHARE_WITH = null
        const val MAJOR_VERSION = -1
        const val MINOR_VERSION = -1
        const val EXTERNAL_WINDOW = false
    }

    val context: GLContext = context.first
    val window: GLWindow? = context.second

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
     *  @param profile Core/Compatibility OpenGL profile:
     *  - GLProfile.Compatibility (default);
     *  - GLProfile.Core.
     *  @param glDebug Creates GLContext that supports debugging
     *  @param shareWith OpenGL context to share with
     *  @param majorVersion Required OpenGL major version
     *  @param minorVersion Required OpenGL minor version
     *  @param externalWindow Creates external window that mirroring canvas. Used to enable debugging via NSight or RenderDoc.
     */
    constructor(
        executor: GLExecutor,
        flipY: Boolean              = Defaults.FLIP_Y,
        msaa: Int                   = Defaults.MSAA,
        fps: Double                 = Defaults.FPS,
        swapBuffers: Int            = Defaults.SWAP_BUFFERS,
        interopType: GLInteropType  = Defaults.INTEROP_TYPE,
        profile: GLProfile          = Defaults.PROFILE,
        glDebug: Boolean            = Defaults.DEBUG,
        shareWith: GLContext?       = Defaults.SHARE_WITH,
        majorVersion: Int           = Defaults.MAJOR_VERSION,
        minorVersion: Int           = Defaults.MINOR_VERSION,
        externalWindow: Boolean     = Defaults.EXTERNAL_WINDOW,
    ): this(
        executor, flipY, msaa,
        fps, swapBuffers, interopType,
        createContextPair(profile, glDebug, shareWith, majorVersion, minorVersion, externalWindow)
    )

    /**
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
     *  @param context Existing context to use (may cause bugs)
     */
    constructor(
        executor: GLExecutor,
        flipY: Boolean              = Defaults.FLIP_Y,
        msaa: Int                   = Defaults.MSAA,
        fps: Double                 = Defaults.FPS,
        swapBuffers: Int            = Defaults.SWAP_BUFFERS,
        interopType: GLInteropType  = Defaults.INTEROP_TYPE,
        context: GLContext
    ): this(
        executor, flipY, msaa,
        fps, swapBuffers, interopType,
        context to null
    )

    /**
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
     *  @param window Existing OpenGL window to use (may cause bugs)
     */
    constructor(
        executor: GLExecutor,
        flipY: Boolean              = Defaults.FLIP_Y,
        msaa: Int                   = Defaults.MSAA,
        fps: Double                 = Defaults.FPS,
        swapBuffers: Int            = Defaults.SWAP_BUFFERS,
        interopType: GLInteropType  = Defaults.INTEROP_TYPE,
        window: GLWindow
    ): this(
        executor, flipY, msaa,
        fps, swapBuffers, interopType,
        window.context to window
    )

    constructor(builder: Builder): this(
        builder.executor ?: throw NullPointerException("Executor can not be null"),
        builder.flipY,
        builder.msaa,
        builder.fps,
        builder.swapBuffers,
        builder.interopType,
        builder.contextDescription.create()
    )

    init {
        object: RegionHelper(){
            init {
                setHelper(this@GLCanvas, this)
            }
            override fun createPeerImpl(node: Node) =
                if(node == this@GLCanvas) doCreatePeer()
                else super.createPeerImpl(node)
        }
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
        while(onInit.isNotEmpty())
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


    /*===========================================*\
    |                    Builder                  |
    \*===========================================*/
    @Suppress("unused")
    class Builder {
        var executor: GLExecutor? = null
            private set

        var flipY: Boolean = Defaults.FLIP_Y
            private set

        var msaa: Int = Defaults.MSAA
            private set

        var fps: Double = Defaults.FPS
            private set

        var swapBuffers: Int = Defaults.SWAP_BUFFERS
            private set

        var interopType: GLInteropType = Defaults.INTEROP_TYPE
            private set

        var contextDescription: ContextDescription = ContextDescription.New()
            private set


        fun setExecutor(value: GLExecutor) = apply {
            this.executor = value
        }

        fun setFlipY(value: Boolean) = apply {
            this.flipY = value
        }

        fun setMSAA(value: Int) = apply {
            this.msaa = value
        }

        fun setFps(value: Double) = apply {
            this.fps = value
        }

        fun setSwapBuffers(value: Int) = apply {
            this.swapBuffers = value
        }

        fun setInteropType(value: GLInteropType) = apply {
            this.interopType = value
        }

        fun setContextDescription(value: ContextDescription) = apply {
            this.contextDescription = value
        }

        interface ContextDescription {
            fun create(): Pair<GLContext, GLWindow?>

            class Existing @JvmOverloads constructor(
                val context: GLContext,
                val window: GLWindow? = null
            ): ContextDescription {
                override fun create(): Pair<GLContext, GLWindow?> =
                    context to window
            }

            class New: ContextDescription {
                var profile: GLProfile = Defaults.PROFILE
                    private set

                var glDebug: Boolean = Defaults.DEBUG
                    private set

                var shareWith: GLContext? = Defaults.SHARE_WITH
                    private set

                var majorVersion: Int = Defaults.MAJOR_VERSION
                    private set

                var minorVersion: Int = Defaults.MINOR_VERSION
                    private set

                var externalWindow: Boolean = Defaults.EXTERNAL_WINDOW
                    private set


                fun setProfile(value: GLProfile) = apply {
                    this.profile = value
                }

                fun setGLDebug(value: Boolean) = apply {
                    this.glDebug = value
                }

                fun setShareWith(value: GLContext) = apply {
                    this.shareWith = value
                }

                fun setMajorVersion(value: Int) = apply {
                    this.majorVersion = value
                }

                fun setMinorVersion(value: Int) = apply {
                    this.minorVersion = value
                }

                fun setExternalWindow(value: Boolean) = apply {
                    this.externalWindow = value
                }

                override fun create(): Pair<GLContext, GLWindow?> =
                    createContextPair(profile, glDebug, shareWith, majorVersion, minorVersion, externalWindow)
            }
        }

        fun build() = GLCanvas(this)
    }
}