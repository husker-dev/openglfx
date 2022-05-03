package com.huskerdev.openglfx.events

import javafx.beans.NamedArg
import javafx.event.Event
import javafx.event.EventType


class GLInitializeEvent(
    @NamedArg("eventType") eventType: EventType<GLInitializeEvent>
) : Event(eventType) {

    companion object {
        private const val serialVersionUID = 20220503L

        /**
         * Common supertype for all event types.
         */
        @JvmStatic
        val ANY: EventType<GLInitializeEvent> = EventType<GLInitializeEvent>(Event.ANY, "GL_INITIALIZE")
    }
}