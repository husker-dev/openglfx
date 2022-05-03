package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.OpenGLCanvas
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2

@JvmField
val JOGL_MODULE = JOGLFXInitializer()

val boundGLThreads = hashMapOf<Thread, GL>()
val contextGL: GL
    get() = boundGLThreads[Thread.currentThread()]!!

abstract class JOGLFXCanvas: OpenGLCanvas() {

    var renderThread: Thread? = null
    val gl: GL2
        get() = boundGLThreads[renderThread] as GL2

    fun fireRenderEvent(gl: GL) {
        boundGLThreads[renderThread!!] = gl
        fireRenderEvent()
        boundGLThreads.remove(renderThread)
    }

    fun fireReshapeEvent(gl: GL, width: Int, height: Int) {
        boundGLThreads[renderThread!!] = gl
        fireReshapeEvent(width, height)
        boundGLThreads.remove(renderThread)
    }

    fun fireInitEvent(gl: GL) {
        boundGLThreads[renderThread!!] = gl
        fireInitEvent()
        boundGLThreads.remove(renderThread)
    }

    fun fireDisposeEvent(gl: GL) {
        boundGLThreads[renderThread!!] = gl
        fireDisposeEvent()
        boundGLThreads.remove(renderThread)
    }
}