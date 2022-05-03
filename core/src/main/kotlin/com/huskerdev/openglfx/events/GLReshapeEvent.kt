package com.huskerdev.openglfx.events

import javafx.beans.NamedArg
import javafx.event.Event
import javafx.event.EventType


class GLReshapeEvent(
    @NamedArg("eventType") eventType: EventType<GLReshapeEvent>,
    val width: Int,
    val height: Int
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