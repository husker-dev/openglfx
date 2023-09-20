package com.huskerdev.openglfx.jogl.events

import com.huskerdev.openglfx.events.GLRenderEvent
import com.jogamp.opengl.GL2
import javafx.beans.NamedArg
import javafx.event.EventType

class JOGLRenderEvent(
    override val gl: GL2,
    @NamedArg("eventType") eventType: EventType<GLRenderEvent>,
    fps: Int,
    delta: Double,
    width: Int,
    height: Int,
    fbo: Int
): GLRenderEvent(eventType, fps, delta, width, height, fbo), JOGLEvent