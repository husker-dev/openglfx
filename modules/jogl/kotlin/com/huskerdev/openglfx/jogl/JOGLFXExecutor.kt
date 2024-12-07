package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import com.huskerdev.openglfx.jogl.events.JOGLDisposeEvent
import com.huskerdev.openglfx.jogl.events.JOGLInitializeEvent
import com.huskerdev.openglfx.jogl.events.JOGLRenderEvent
import com.huskerdev.openglfx.jogl.events.JOGLReshapeEvent
import com.jogamp.opengl.GL3
import jogamp.opengl.GLDrawableFactoryImpl

@JvmField @Suppress("unused")
val JOGL_MODULE = JOGLExecutor()

class JOGLExecutor: GLExecutor() {

    private val gl = hashMapOf<GLCanvas, GL3>()

    override fun createRenderEvent(canvas: GLCanvas, currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int) =
        JOGLRenderEvent(gl[canvas]!!, GLRenderEvent.ANY, currentFps, delta, width, height, fbo)

    override fun createReshapeEvent(canvas: GLCanvas, width: Int, height: Int) =
        JOGLReshapeEvent(gl[canvas]!!, GLReshapeEvent.ANY, width, height)

    override fun createInitEvent(canvas: GLCanvas): GLInitializeEvent {
        if(!gl.containsKey(canvas))
            gl[canvas] = GLDrawableFactoryImpl.getFactoryImpl(com.jogamp.opengl.GLProfile.getDefault()).createExternalGLContext().gl.gL3
        return JOGLInitializeEvent(gl[canvas]!!, GLInitializeEvent.ANY)
    }

    override fun createDisposeEvent(canvas: GLCanvas): GLDisposeEvent {
        val gl = gl.remove(canvas)
        return JOGLDisposeEvent(gl!!, GLDisposeEvent.ANY)
    }
}