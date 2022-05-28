package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.core.GLExecutor
import com.huskerdev.openglfx.core.implementation.*
import com.huskerdev.openglfx.events.*
import com.huskerdev.openglfx.jogl.events.*
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLProfile
import jogamp.opengl.GLDrawableFactoryImpl

class JOGLUniversalCanvas(
    executor: GLExecutor,
    profile: Int
): UniversalImpl(executor, profile){

    var gl: GL2? = null
        get() {
            if(field == null)
                field = GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL2
            return field
        }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int) = JOGLRenderEvent(gl!!, GLRenderEvent.ANY, currentFps, delta, width, height)
    override fun createReshapeEvent(width: Int, height: Int) = JOGLReshapeEvent(gl!!, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() = JOGLInitializeEvent(gl!!, GLInitializeEvent.ANY)
    override fun createDisposeEvent() = JOGLDisposeEvent(gl!!, GLDisposeEvent.ANY)
}

class JOGLSharedCanvas(
    executor: GLExecutor,
    profile: Int
): SharedImpl(executor, profile){

    var gl: GL2? = null
        get() {
            if(field == null)
                field = GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL2
            return field
        }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int) = JOGLRenderEvent(gl!!, GLRenderEvent.ANY, currentFps, delta, width, height)
    override fun createReshapeEvent(width: Int, height: Int) = JOGLReshapeEvent(gl!!, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() = JOGLInitializeEvent(gl!!, GLInitializeEvent.ANY)
    override fun createDisposeEvent() = JOGLDisposeEvent(gl!!, GLDisposeEvent.ANY)
}

class JOGLInteropCanvas(
    executor: GLExecutor,
    profile: Int
): InteropImpl(executor, profile){

    var gl: GL2? = null
        get() {
            if(field == null)
                field = GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL2
            return field
        }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int) = JOGLRenderEvent(gl!!, GLRenderEvent.ANY, currentFps, delta, width, height)
    override fun createReshapeEvent(width: Int, height: Int) = JOGLReshapeEvent(gl!!, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() = JOGLInitializeEvent(gl!!, GLInitializeEvent.ANY)
    override fun createDisposeEvent() = JOGLDisposeEvent(gl!!, GLDisposeEvent.ANY)
}