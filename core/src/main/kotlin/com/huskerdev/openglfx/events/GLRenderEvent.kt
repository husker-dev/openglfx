package com.huskerdev.openglfx.events

import javafx.beans.NamedArg
import javafx.event.Event
import javafx.event.EventType


class GLRenderEvent(
    @NamedArg("eventType") eventType: EventType<GLRenderEvent>,
    val fps: Double,
    val delta: Int
) : Event(eventType) {

    companion object {
        private const val serialVersionUID = 20220503L

        /**
         * Common supertype for all event types.
         */
        @JvmStatic
        val ANY: EventType<GLRenderEvent> = EventType<GLRenderEvent>(Event.ANY, "GL_RENDER")
    }
}