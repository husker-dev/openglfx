package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.events.*
import com.huskerdev.openglfx.jogl.events.*
import com.jogamp.opengl.GL2
import javafx.event.EventType

@JvmField
val JOGL_MODULE = JOGLFXInitializer()

abstract class JOGLFXCanvas: OpenGLCanvas() {
    abstract val gl: GL2

    override fun dispatchRenderEvent(event: GLRenderEvent) =
        super.dispatchRenderEvent(JOGLRenderEvent(gl, event.eventType as EventType<GLRenderEvent>, event.fps, event.delta))

    override fun dispatchReshapeEvent(event: GLReshapeEvent) =
        super.dispatchReshapeEvent(JOGLReshapeEvent(gl, event.eventType as EventType<GLReshapeEvent>, event.width, event.height))

    override fun dispatchInitEvent(event: GLInitializeEvent) =
        super.dispatchInitEvent(JOGLInitializeEvent(gl, event.eventType as EventType<GLInitializeEvent>))

    override fun dispatchDisposeEvent(event: GLDisposeEvent) =
        super.dispatchDisposeEvent(JOGLDisposeEvent(gl, event.eventType as EventType<GLDisposeEvent>))
}