package com.huskerdev.openglfx.canvas.events

import javafx.beans.NamedArg
import javafx.event.Event
import javafx.event.EventType


open class GLReshapeEvent(
    @NamedArg("eventType") eventType: EventType<GLReshapeEvent>,
    @JvmField val width: Int,
    @JvmField val height: Int
) : Event(eventType) {

    companion object {
        private const val serialVersionUID = 20220503L

        /**
         * Common supertype for all event types.
         */
        @JvmStatic
        val ANY: EventType<GLReshapeEvent> = EventType<GLReshapeEvent>(Event.ANY, "GL_RESHAPE")
    }
}