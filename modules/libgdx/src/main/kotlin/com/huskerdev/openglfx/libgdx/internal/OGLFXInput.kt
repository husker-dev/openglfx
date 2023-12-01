package com.huskerdev.openglfx.libgdx.internal

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.huskerdev.openglfx.canvas.GLCanvas
import javafx.event.EventHandler
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent


class OGLFXInput(canvas: GLCanvas): Input {

    private var processor: InputProcessor? = null

    private lateinit var mouseEvent: MouseEvent
    private lateinit var prevMouseEvent: MouseEvent

    private lateinit var keyEvent: KeyEvent
    private lateinit var prevKeyEvent: KeyEvent
    private val pressedKeys = arrayListOf<Int>()

    init {
        canvas.onMousePressed = EventHandler {
            processor?.touchDown(it.x.toInt(), it.y.toInt(), 1, getPressedButtonIndex(it))
            handleMouseEvent(it)
        }
        canvas.onMouseReleased = EventHandler {
            processor?.touchUp(it.x.toInt(), it.y.toInt(), 1, getPressedButtonIndex(it))
            handleMouseEvent(it)
        }
        canvas.onMouseClicked = EventHandler(::handleMouseEvent)
        canvas.onMouseEntered = EventHandler(::handleMouseEvent)
        canvas.onMouseExited = EventHandler(::handleMouseEvent)
        canvas.onMouseMoved = EventHandler {
            processor?.mouseMoved(it.x.toInt(), it.y.toInt())
            handleMouseEvent(it)
        }
        canvas.onMouseDragged = EventHandler {
            processor?.touchDragged(it.x.toInt(), it.y.toInt(), 1)
            handleMouseEvent(it)
        }
        canvas.onMouseDragEntered = EventHandler(::handleMouseEvent)
        canvas.onMouseDragReleased = EventHandler(::handleMouseEvent)
        canvas.onMouseDragOver = EventHandler(::handleMouseEvent)
        canvas.onMouseDragExited = EventHandler(::handleMouseEvent)

        canvas.onKeyPressed = EventHandler {
            pressedKeys.add(it.code.code)
            processor?.keyDown(it.code.code)
            handleKeyEvent(it)
        }
        canvas.onKeyReleased = EventHandler {
            pressedKeys.remove(it.code.code)
            processor?.keyUp(it.code.code)
            handleKeyEvent(it)
        }
        canvas.onKeyTyped = EventHandler{
            processor?.keyTyped(it.character[0])
            handleKeyEvent(it)
        }

        canvas.onScroll = EventHandler {
            processor?.scrolled(it.deltaX.toFloat(), it.deltaY.toFloat())
        }
    }

    private fun getPressedButtonIndex(event: MouseEvent) = when {
        event.isPrimaryButtonDown -> Input.Buttons.LEFT
        event.isSecondaryButtonDown -> Input.Buttons.RIGHT
        event.isMiddleButtonDown -> Input.Buttons.MIDDLE
        event.isBackButtonDown -> Input.Buttons.BACK
        event.isForwardButtonDown -> Input.Buttons.FORWARD
        else -> -1
    }

    private fun handleMouseEvent(event: MouseEvent){
        if(::mouseEvent.isInitialized)
            prevMouseEvent = mouseEvent
        mouseEvent = event
    }

    private fun handleKeyEvent(event: KeyEvent){
        if(::keyEvent.isInitialized)
            prevKeyEvent = keyEvent
        keyEvent = event
    }

    override fun getAccelerometerX() = 0f
    override fun getAccelerometerY() = 0f
    override fun getAccelerometerZ() = 0f

    override fun getGyroscopeX() = 0f
    override fun getGyroscopeY() = 0f
    override fun getGyroscopeZ() = 0f

    override fun getMaxPointers() = 1

    override fun getX() = mouseEvent.x.toInt()
    override fun getX(pointer: Int) = getX()

    override fun getDeltaX() = if(::prevMouseEvent.isInitialized)
            (mouseEvent.x - prevMouseEvent.x).toInt() else 0
    override fun getDeltaX(pointer: Int) = getDeltaX()

    override fun getY() = mouseEvent.y.toInt()
    override fun getY(pointer: Int) = getY()

    override fun getDeltaY() = if(::prevMouseEvent.isInitialized)
        (mouseEvent.y - prevMouseEvent.y).toInt() else 0
    override fun getDeltaY(pointer: Int) = getDeltaY()

    override fun isTouched() =
        mouseEvent.isPrimaryButtonDown ||
        mouseEvent.isSecondaryButtonDown ||
        mouseEvent.isMiddleButtonDown
    override fun isTouched(pointer: Int) = isTouched()

    override fun justTouched() = isTouched() !=
        prevMouseEvent.isPrimaryButtonDown ||
        prevMouseEvent.isSecondaryButtonDown ||
        prevMouseEvent.isMiddleButtonDown

    override fun getPressure() = 1f
    override fun getPressure(pointer: Int) = getPressure()

    override fun isButtonPressed(button: Int) = when(button){
        Input.Buttons.LEFT -> mouseEvent.isPrimaryButtonDown
        Input.Buttons.RIGHT -> mouseEvent.isSecondaryButtonDown
        Input.Buttons.MIDDLE -> mouseEvent.isMiddleButtonDown
        Input.Buttons.BACK -> mouseEvent.isBackButtonDown
        Input.Buttons.FORWARD -> mouseEvent.isForwardButtonDown
        else -> false
    }

    override fun isButtonJustPressed(button: Int) = isButtonPressed(button) !=
        when(button){
            Input.Buttons.LEFT -> prevMouseEvent.isPrimaryButtonDown
            Input.Buttons.RIGHT -> prevMouseEvent.isSecondaryButtonDown
            Input.Buttons.MIDDLE -> prevMouseEvent.isMiddleButtonDown
            Input.Buttons.BACK -> prevMouseEvent.isBackButtonDown
            Input.Buttons.FORWARD -> prevMouseEvent.isForwardButtonDown
            else -> false
        }

    override fun isKeyPressed(key: Int) = pressedKeys.contains(key)
    override fun isKeyJustPressed(key: Int) = false

    override fun getTextInput(listener: Input.TextInputListener?, title: String?, text: String?, hint: String?) {
        val dialog = TextInputDialog()
        dialog.title = title
        dialog.headerText = text
        dialog.setOnCloseRequest { listener?.canceled() }
        dialog.showAndWait()
        if(dialog.editor.text != null)
            listener?.input(dialog.editor.text)
    }

    override fun getTextInput(
        listener: Input.TextInputListener?,
        title: String?,
        text: String?,
        hint: String?,
        type: Input.OnscreenKeyboardType?
    ) = getTextInput(listener, title, text, null)

    override fun setInputProcessor(processor: InputProcessor?) {
        this.processor = processor
    }
    override fun getInputProcessor() = processor

    override fun setOnscreenKeyboardVisible(visible: Boolean) {}
    override fun setOnscreenKeyboardVisible(visible: Boolean, type: Input.OnscreenKeyboardType?) {}
    override fun vibrate(milliseconds: Int) {}
    override fun vibrate(milliseconds: Int, fallback: Boolean) {}
    override fun vibrate(milliseconds: Int, amplitude: Int, fallback: Boolean) {}
    override fun vibrate(vibrationType: Input.VibrationType?) {}

    override fun getAzimuth() = 0f
    override fun getPitch() = 0f
    override fun getRoll() = 0f
    override fun getRotationMatrix(matrix: FloatArray?) {}

    override fun getCurrentEventTime() = System.currentTimeMillis()

    @Deprecated("Deprecated in Java")
    override fun setCatchBackKey(catchBack: Boolean) { }

    @Deprecated("Deprecated in Java")
    override fun isCatchBackKey() = true

    @Deprecated("Deprecated in Java")
    override fun setCatchMenuKey(catchMenu: Boolean) {}

    @Deprecated("Deprecated in Java")
    override fun isCatchMenuKey() = true

    override fun setCatchKey(keycode: Int, catchKey: Boolean) {}
    override fun isCatchKey(keycode: Int) = true

    override fun isPeripheralAvailable(peripheral: Input.Peripheral?) = true
    override fun getRotation() = 0
    override fun getNativeOrientation() = Input.Orientation.Landscape

    override fun setCursorCatched(catched: Boolean) {}
    override fun isCursorCatched() = true
    override fun setCursorPosition(x: Int, y: Int) {}
}