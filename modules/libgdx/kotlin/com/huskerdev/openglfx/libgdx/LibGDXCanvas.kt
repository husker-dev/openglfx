package com.huskerdev.openglfx.libgdx

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.libgdx.LibGDXExecutor.Companion.LIBGDX_MODULE
import com.huskerdev.openglfx.libgdx.internal.OGLFXApplication

class LibGDXCanvas(
    val adapter: ApplicationAdapter,
    val configuration: OGLFXApplicationConfiguration = OGLFXApplicationConfiguration(),
    flipY: Boolean              = false,
    msaa: Int                   = 0,
    fxaa: Boolean               = false,
    async: Boolean              = false,
    interopType: GLInteropType = GLInteropType.auto
): GLCanvas(
    LIBGDX_MODULE,
    GLProfile.CORE,
    flipY, msaa, fxaa, async, interopType
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
        }
        adapter.render()
    }

    override fun fireInitEvent() {
        super.fireInitEvent()
        if(!::application.isInitialized) {
            application = OGLFXApplication(configuration, this)
            adapter.create()
        }
    }

    override fun fireDisposeEvent() {
        super.fireDisposeEvent()
        if(::application.isInitialized)
            adapter.dispose()
    }

    fun invokeLater(runnable: Runnable){
        synchronized(invokeLater){
            invokeLater.add(runnable)
        }
    }
}