package com.huskerdev.openglfx.jogl.events

import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.jogamp.opengl.GL3
import javafx.beans.NamedArg
import javafx.event.EventType

class JOGLDisposeEvent (
    override val gl: GL3,
    @NamedArg("eventType") eventType: EventType<GLDisposeEvent>
): GLDisposeEvent(eventType), JOGLEvent