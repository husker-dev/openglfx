package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.core.GLExecutor
import com.huskerdev.openglfx.core.impl.*
import com.huskerdev.openglfx.events.*
import com.huskerdev.openglfx.jogl.events.*
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLProfile
import javafx.event.EventType
import jogamp.opengl.GLDrawableFactoryImpl

class JOGLUniversalCanvas(executor: GLExecutor): UniversalGLCanvas(executor){

    var gl: GL2? = null
        get() {
            if(field == null)
                field = GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL2
            return field
        }

    override fun dispatchRenderEvent(event: GLRenderEvent) =
        super.dispatchRenderEvent(JOGLRenderEvent(gl!!, event.eventType as EventType<GLRenderEvent>, event.fps, event.delta))

    override fun dispatchReshapeEvent(event: GLReshapeEvent) =
        super.dispatchReshapeEvent(JOGLReshapeEvent(gl!!, event.eventType as EventType<GLReshapeEvent>, event.width, event.height))

    override fun dispatchInitEvent(event: GLInitializeEvent) =
        super.dispatchInitEvent(JOGLInitializeEvent(gl!!, event.eventType as EventType<GLInitializeEvent>))

    override fun dispatchDisposeEvent(event: GLDisposeEvent) =
        super.dispatchDisposeEvent(JOGLDisposeEvent(gl!!, event.eventType as EventType<GLDisposeEvent>))
}

class JOGLSharedCanvas(executor: GLExecutor): SharedGLCanvas(executor){

    var gl: GL2? = null
        get() {
            if(field == null)
                field = GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL2
            return field
        }

    override fun dispatchRenderEvent(event: GLRenderEvent) =
        super.dispatchRenderEvent(JOGLRenderEvent(gl!!, event.eventType as EventType<GLRenderEvent>, event.fps, event.delta))

    override fun dispatchReshapeEvent(event: GLReshapeEvent) =
        super.dispatchReshapeEvent(JOGLReshapeEvent(gl!!, event.eventType as EventType<GLReshapeEvent>, event.width, event.height))

    override fun dispatchInitEvent(event: GLInitializeEvent) =
        super.dispatchInitEvent(JOGLInitializeEvent(gl!!, event.eventType as EventType<GLInitializeEvent>))

    override fun dispatchDisposeEvent(event: GLDisposeEvent) =
        super.dispatchDisposeEvent(JOGLDisposeEvent(gl!!, event.eventType as EventType<GLDisposeEvent>))
}

class JOGLInteropCanvas(executor: GLExecutor): InteropGLCanvas(executor){

    var gl: GL2? = null
        get() {
            if(field == null)
                field = GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL2
            return field
        }

    override fun dispatchRenderEvent(event: GLRenderEvent) =
        super.dispatchRenderEvent(JOGLRenderEvent(gl!!, event.eventType as EventType<GLRenderEvent>, event.fps, event.delta))

    override fun dispatchReshapeEvent(event: GLReshapeEvent) =
        super.dispatchReshapeEvent(JOGLReshapeEvent(gl!!, event.eventType as EventType<GLReshapeEvent>, event.width, event.height))

    override fun dispatchInitEvent(event: GLInitializeEvent) =
        super.dispatchInitEvent(JOGLInitializeEvent(gl!!, event.eventType as EventType<GLInitializeEvent>))

    override fun dispatchDisposeEvent(event: GLDisposeEvent) =
        super.dispatchDisposeEvent(JOGLDisposeEvent(gl!!, event.eventType as EventType<GLDisposeEvent>))
}