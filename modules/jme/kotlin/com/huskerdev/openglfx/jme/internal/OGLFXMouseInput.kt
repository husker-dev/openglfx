package com.huskerdev.openglfx.jme.internal

import com.jme3.cursors.plugins.JmeCursor
import com.jme3.input.MouseInput
import com.jme3.input.MouseInput.*
import com.jme3.input.event.MouseButtonEvent
import com.jme3.input.event.MouseMotionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import java.util.LinkedList

class OGLFXMouseInput(
    context: OGLFXSurfaceContext
): OGLFXInput(context), MouseInput {

    companion object {
        private val MOUSE_BUTTON_TO_JME = hashMapOf(
            MouseButton.PRIMARY     to BUTTON_LEFT,
            MouseButton.SECONDARY   to BUTTON_RIGHT,
            MouseButton.MIDDLE      to BUTTON_MIDDLE
        )
    }

    private var lastX = -1
    private var lastY = -1
    private var wheelValue = 0.0

    private val mouseMotionEvents = LinkedList<MouseMotionEvent>()
    private val mouseButtonEvents = LinkedList<MouseButtonEvent>()

    private val moveListener = EventHandler<MouseEvent> {
        val x = it.x.toInt()
        val y = it.y.toInt()

        val deltaX = if(lastX == -1) x else (x - lastX)
        val deltaY = if(lastY == -1) y else (y - lastY)
        lastX = x
        lastY = y

        if (deltaX == 0 && deltaY == 0)
            return@EventHandler

        val mouseMotionEvent = MouseMotionEvent(x, y, deltaX, deltaY, wheelValue.toInt(), 0)
        mouseMotionEvent.time = getInputTimeNanos()
        mouseMotionEvents.add(mouseMotionEvent)
    }

    private val pressListener = EventHandler<MouseEvent> {
        val mouseButtonEvent = MouseButtonEvent(
            MOUSE_BUTTON_TO_JME[it.button] ?: 0, true, it.x.toInt(), it.y.toInt())
        mouseButtonEvent.setTime(getInputTimeNanos());
        mouseButtonEvents.add(mouseButtonEvent)
    }

    private val releaseListener = EventHandler<MouseEvent> {
        val mouseButtonEvent = MouseButtonEvent(
            MOUSE_BUTTON_TO_JME[it.button] ?: 0, false, it.x.toInt(), it.y.toInt())
        mouseButtonEvent.setTime(getInputTimeNanos());
        mouseButtonEvents.add(mouseButtonEvent)
    }

    private val scrollListener = EventHandler<ScrollEvent> {
        val delta = it.deltaY * 10
        wheelValue += delta

        val mouseMotionEvent = MouseMotionEvent(
            it.x.toInt(), it.y.toInt(), 0, 0, wheelValue.toInt(), delta.toInt())
        mouseMotionEvent.setTime(getInputTimeNanos());
        mouseMotionEvents.add(mouseMotionEvent)
    }

    override fun bind(node: Node) {
        super.bind(node)
        node.addEventHandler(MouseEvent.MOUSE_MOVED, moveListener)
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, pressListener)
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseListener)
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, moveListener)
        node.addEventHandler(ScrollEvent.ANY, scrollListener)
    }

    override fun unbind() {
        super.unbind()
        node?.removeEventHandler(MouseEvent.MOUSE_MOVED, moveListener)
        node?.removeEventHandler(MouseEvent.MOUSE_DRAGGED, pressListener)
        node?.removeEventHandler(MouseEvent.MOUSE_PRESSED, releaseListener)
        node?.removeEventHandler(MouseEvent.MOUSE_RELEASED, moveListener)
        node?.removeEventHandler(ScrollEvent.ANY, scrollListener)
    }

    override fun update() {
        while (!mouseMotionEvents.isEmpty()) {
            listener?.onMouseMotionEvent(mouseMotionEvents.poll());
        }

        while (!mouseButtonEvents.isEmpty()) {
            listener?.onMouseButtonEvent(mouseButtonEvents.poll());
        }
    }

    override fun setCursorVisible(visible: Boolean) {
        // TODO: Implement
    }

    override fun setNativeCursor(cursor: JmeCursor) {
        // TODO: Implement
    }

    override fun getButtonCount() = 3
}