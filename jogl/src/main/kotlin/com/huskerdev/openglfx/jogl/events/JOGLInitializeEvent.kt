package com.huskerdev.openglfx.jogl.events

import com.huskerdev.openglfx.events.GLInitializeEvent
import com.jogamp.opengl.GL2
import javafx.beans.NamedArg
import javafx.event.EventType

class JOGLInitializeEvent(
    val gl: GL2,
    @NamedArg("eventType") eventType: EventType<GLInitializeEvent>
): GLInitializeEvent(eventType)