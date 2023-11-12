package com.huskerdev.openglfx.canvas.events

import javafx.beans.NamedArg
import javafx.event.Event
import javafx.event.EventType

open class GLDisposeEvent(
    @NamedArg("eventType") eventType: EventType<GLDisposeEvent>
) : Event(eventType) {

    companion object {
        private const val serialVersionUID = 20220503L

        /**
         * Common supertype for all event types.
         */
        @JvmStatic
        val ANY: EventType<GLDisposeEvent> = EventType<GLDisposeEvent>(Event.ANY, "GL_DISPOSE")
    }
}