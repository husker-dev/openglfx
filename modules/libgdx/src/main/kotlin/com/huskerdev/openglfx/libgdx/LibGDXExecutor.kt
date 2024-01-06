package com.huskerdev.openglfx.libgdx

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.libgdx.events.LibGDXDisposeEvent
import com.huskerdev.openglfx.libgdx.events.LibGDXInitializeEvent
import com.huskerdev.openglfx.libgdx.events.LibGDXRenderEvent
import com.huskerdev.openglfx.libgdx.events.LibGDXReshapeEvent
import org.lwjgl.opengl.GL

class LibGDXExecutor: GLExecutor() {

    companion object {
        val LIBGDX_MODULE = LibGDXExecutor()
    }

    override fun createRenderEvent(canvas: GLCanvas, currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int) =
        LibGDXRenderEvent((canvas as LibGDXCanvas).application, currentFps, delta, width, height, fbo)

    override fun createReshapeEvent(canvas: GLCanvas, width: Int, height: Int) =
        LibGDXReshapeEvent((canvas as LibGDXCanvas).application, width, height)

    override fun createInitEvent(canvas: GLCanvas) =
        LibGDXInitializeEvent((canvas as LibGDXCanvas).application)

    override fun createDisposeEvent(canvas: GLCanvas) =
        LibGDXDisposeEvent((canvas as LibGDXCanvas).application)

    override fun initGLFunctions() {
        super.initGLFunctions()
        GL.createCapabilities()
    }
}