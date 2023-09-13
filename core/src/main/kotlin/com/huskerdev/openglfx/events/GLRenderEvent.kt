package com.huskerdev.openglfx.events

import javafx.beans.NamedArg
import javafx.event.Event
import javafx.event.EventType


open class GLRenderEvent(
    @NamedArg("eventType") eventType: EventType<GLRenderEvent>,
    @JvmField val fps: Int,
    @JvmField val delta: Double,
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val fbo: Int,
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