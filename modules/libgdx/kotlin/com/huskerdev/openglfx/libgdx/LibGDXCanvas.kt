package com.huskerdev.openglfx.libgdx

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.huskerdev.grapl.gl.GLContext
import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.libgdx.LibGDXExecutor.Companion.LIBGDX_MODULE
import com.huskerdev.openglfx.libgdx.internal.OGLFXApplication
import kotlin.Boolean

class LibGDXCanvas(
    val adapter: ApplicationAdapter,
    val configuration: OGLFXApplicationConfiguration = OGLFXApplicationConfiguration(),
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
): GLCanvas(
    LIBGDX_MODULE,
    flipY, msaa, fps, swapBuffers, interopType,
    profile, glDebug, shareWith, majorVersion, minorVersion, externalWindow
) {
    private val invokeLater = arrayListOf<Runnable>()
    lateinit var application: Application
        private set

    init {
        focusTraversableProperty().set(true)
    }

    override fun fireRenderEvent(fbo: Int) {
        super.fireRenderEvent(fbo)
        synchronized(invokeLater){
            invokeLater.forEach { it.run() }
            invokeLater.clear()
        }
        adapter.render()
    }

    override fun fireReshapeEvent(width: Int, height: Int) {
        super.fireReshapeEvent(width, height)
        adapter.resize(width, height)
    }

    override fun fireInitEvent() {
        if(!::application.isInitialized) {
            application = OGLFXApplication(configuration, this)
            adapter.create()
        }
        super.fireInitEvent()
    }

    override fun fireDisposeEvent() {
        if(::application.isInitialized)
            adapter.dispose()
        super.fireDisposeEvent()
    }

    fun invokeLater(runnable: Runnable){
        synchronized(invokeLater){
            invokeLater.add(runnable)
        }
    }
}