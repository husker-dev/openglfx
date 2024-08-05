package com.huskerdev.openglfx.libgdx.internal

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.huskerdev.openglfx.canvas.GLCanvas
import javafx.event.EventHandler
import javafx.scene.control.TextInputDialog
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent


private fun <T> copySet(target: HashSet<T>, src: HashSet<T>){
    target.clear()
    src.forEach { target.add(it) }
}

private fun fxToGDXKey(key: MouseButton) = when(key){
    MouseButton.PRIMARY -> Input.Buttons.LEFT
    MouseButton.MIDDLE -> Input.Buttons.MIDDLE
    MouseButton.SECONDARY -> Input.Buttons.RIGHT
    MouseButton.BACK -> Input.Buttons.BACK
    MouseButton.FORWARD -> Input.Buttons.FORWARD
    MouseButton.NONE -> throw UnsupportedOperationException("JavaFX key is MouseButton.NONE")
}

class OGLFXInput(canvas: GLCanvas): Input {
    private var processor: InputProcessor? = null

    private var x = 0
    private var y = 0
    private var deltaX = 0
    private var deltaY = 0
    private val pressedMouseButtons = hashSetOf<Int>()
    private val pressedMouseButtonsPast = hashSetOf<Int>()

    private val pressedKeys = hashSetOf<Int>()
    private val pressedKeysPast = hashSetOf<Int>()

    init {
        canvas.onMousePressed = EventHandler {
            canvas.requestFocus()
            val key = fxToGDXKey(it.button)
            processor?.touchDown(it.x.toInt(), it.y.toInt(), 1, key)
            copySet(pressedMouseButtonsPast, pressedMouseButtons)
            pressedMouseButtons.add(key)
        }
        canvas.onMouseReleased = EventHandler {
            val key = fxToGDXKey(it.button)
            processor?.touchUp(it.x.toInt(), it.y.toInt(), 1, key)
            copySet(pressedMouseButtonsPast, pressedMouseButtons)
            pressedMouseButtons.remove(key)
        }
        canvas.onMouseMoved = EventHandler {
            processor?.mouseMoved(it.x.toInt(), it.y.toInt())
            handleMouseMoveEvent(it)
        }
        canvas.onMouseDragged = EventHandler {
            processor?.touchDragged(it.x.toInt(), it.y.toInt(), 1)
            handleMouseMoveEvent(it)
        }

        canvas.onKeyPressed = EventHandler {
            val key = it.code.code
            if(!pressedKeys.contains(key))
                processor?.keyDown(key)
            copySet(pressedKeysPast, pressedKeys)
            pressedKeys.add(key)
        }
        canvas.onKeyReleased = EventHandler {
            val key = it.code.code
            processor?.keyUp(key)
            copySet(pressedKeysPast, pressedKeys)
            pressedKeys.remove(key)
        }
        canvas.onKeyTyped = EventHandler{
            processor?.keyTyped(it.character[0])
        }

        canvas.onScroll = EventHandler {
            processor?.scrolled(it.deltaX.toFloat(), it.deltaY.toFloat())
        }
    }

    private fun handleMouseMoveEvent(event: MouseEvent){
        deltaX = event.x.toInt() - x
        deltaY = event.y.toInt() - y
        x = event.x.toInt()
        y = event.y.toInt()
    }

    override fun getAccelerometerX() = 0f
    override fun getAccelerometerY() = 0f
    override fun getAccelerometerZ() = 0f

    override fun getGyroscopeX() = 0f
    override fun getGyroscopeY() = 0f
    override fun getGyroscopeZ() = 0f

    override fun getMaxPointers() = 1

    override fun getX() = x
    override fun getX(pointer: Int) = x

    override fun getDeltaX() = deltaX
    override fun getDeltaX(pointer: Int) = deltaX

    override fun getY() = y
    override fun getY(pointer: Int) = y

    override fun getDeltaY() = deltaY
    override fun getDeltaY(pointer: Int) = deltaY

    override fun isTouched() = pressedMouseButtons.isNotEmpty()
    override fun isTouched(pointer: Int) = pressedMouseButtons.isNotEmpty()

    override fun justTouched() = pressedMouseButtons.size > pressedMouseButtonsPast.size

    override fun getPressure() = 1f
    override fun getPressure(pointer: Int) = getPressure()

    override fun isButtonPressed(button: Int) = pressedMouseButtons.contains(button)

    override fun isButtonJustPressed(button: Int) =
        pressedMouseButtons.contains(button) && !pressedMouseButtonsPast.contains(button)

    override fun isKeyPressed(key: Int) = pressedKeys.contains(key)
    override fun isKeyJustPressed(key: Int) = pressedKeys.contains(key) && !pressedKeysPast.contains(key)

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