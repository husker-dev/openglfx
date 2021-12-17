package com.husker.openglfx.jogl

import com.husker.openglfx.OpenGLCanvas
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2

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

    fun fireReshapeEvent(gl: GL) {
        boundGLThreads[renderThread!!] = gl
        fireReshapeEvent()
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