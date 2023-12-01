package com.huskerdev.openglfx.libgdx

import com.badlogic.gdx.Application
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.libgdx.events.LibGDXDisposeEvent
import com.huskerdev.openglfx.libgdx.events.LibGDXInitializeEvent
import com.huskerdev.openglfx.libgdx.events.LibGDXRenderEvent
import com.huskerdev.openglfx.libgdx.events.LibGDXReshapeEvent
import com.huskerdev.openglfx.libgdx.internal.OGLFXApplication
import org.lwjgl.opengl.GL

class LibGDXExecutor(private val configuration: OGLFXApplicationConfiguration): GLExecutor() {

    companion object {
        val LIBGDX_MODULE = LibGDXExecutor(OGLFXApplicationConfiguration())

        fun LIBGDX_MODULE(configuration: OGLFXApplicationConfiguration) = LibGDXExecutor(configuration)
    }

    internal val invokeLater = arrayListOf<Runnable>()
    private val applications = hashMapOf<GLCanvas, Application>()

    override fun createRenderEvent(canvas: GLCanvas, currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int): GLRenderEvent {
        synchronized(invokeLater){
            invokeLater.forEach { it.run() }
        }
        return LibGDXRenderEvent(applications[canvas]!!, currentFps, delta, width, height, fbo)
    }

    override fun createReshapeEvent(canvas: GLCanvas, width: Int, height: Int) =
        LibGDXReshapeEvent(applications[canvas]!!, width, height)

    override fun createInitEvent(canvas: GLCanvas): GLInitializeEvent {
        if(!applications.containsKey(canvas))
            applications[canvas] = OGLFXApplication(configuration, canvas, this)
        return LibGDXInitializeEvent(applications[canvas]!!)
    }

    override fun createDisposeEvent(canvas: GLCanvas): GLDisposeEvent {
        if(applications.containsKey(canvas))
            applications.remove(canvas)
        return LibGDXDisposeEvent(applications[canvas]!!)
    }

    override fun initGLFunctions() {
        super.initGLFunctions()
        GL.createCapabilities()
    }
}