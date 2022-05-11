package com.huskerdev.openglfx.jogl.events

import com.huskerdev.openglfx.events.GLReshapeEvent
import com.jogamp.opengl.GL2
import javafx.beans.NamedArg
import javafx.event.EventType

class JOGLReshapeEvent(
    val gl: GL2,
    @NamedArg("eventType") eventType: EventType<GLReshapeEvent>,
    width: Int,
    height: Int
): GLReshapeEvent(eventType, width, height)