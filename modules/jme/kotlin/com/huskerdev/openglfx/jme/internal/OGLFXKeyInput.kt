package com.huskerdev.openglfx.jme.internal

import com.jme3.input.KeyInput
import com.jme3.input.KeyInput.*
import com.jme3.input.event.KeyInputEvent
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import java.util.LinkedList

class OGLFXKeyInput(
    context: OGLFXSurfaceContext
): OGLFXInput(context), KeyInput {
    companion object {
        private val KEY_CODE_TO_JME = hashMapOf(
            KeyCode.ESCAPE          to KEY_ESCAPE,
            KeyCode.DIGIT0          to KEY_0,
            KeyCode.DIGIT1          to KEY_1,
            KeyCode.DIGIT2          to KEY_2,
            KeyCode.DIGIT3          to KEY_3,
            KeyCode.DIGIT4          to KEY_4,
            KeyCode.DIGIT5          to KEY_5,
            KeyCode.DIGIT6          to KEY_6,
            KeyCode.DIGIT7          to KEY_7,
            KeyCode.DIGIT8          to KEY_8,
            KeyCode.DIGIT9          to KEY_9,
            KeyCode.MINUS           to KEY_MINUS,
            KeyCode.EQUALS          to KEY_EQUALS,
            KeyCode.BACK_SPACE      to KEY_BACK,
            KeyCode.TAB             to KEY_TAB,
            KeyCode.Q               to KEY_Q,
            KeyCode.W               to KEY_W,
            KeyCode.E               to KEY_E,
            KeyCode.R               to KEY_R,
            KeyCode.T               to KEY_T,
            KeyCode.U               to KEY_U,
            KeyCode.I               to KEY_I,
            KeyCode.O               to KEY_O,
            KeyCode.P               to KEY_P,
            KeyCode.OPEN_BRACKET    to KEY_LBRACKET,
            KeyCode.CLOSE_BRACKET   to KEY_RBRACKET,
            KeyCode.ENTER           to KEY_RETURN,
            KeyCode.CONTROL         to KEY_LCONTROL,
            KeyCode.A               to KEY_A,
            KeyCode.S               to KEY_S,
            KeyCode.D               to KEY_D,
            KeyCode.F               to KEY_F,
            KeyCode.G               to KEY_G,
            KeyCode.H               to KEY_H,
            KeyCode.J               to KEY_J,
            KeyCode.Y               to KEY_Y,
            KeyCode.K               to KEY_K,
            KeyCode.L               to KEY_L,
            KeyCode.SEMICOLON       to KEY_SEMICOLON,
            KeyCode.QUOTE           to KEY_APOSTROPHE,
            KeyCode.DEAD_GRAVE      to KEY_GRAVE,
            KeyCode.SHIFT           to KEY_LSHIFT,
            KeyCode.BACK_SLASH      to KEY_BACKSLASH,
            KeyCode.Z               to KEY_Z,
            KeyCode.X               to KEY_X,
            KeyCode.C               to KEY_C,
            KeyCode.V               to KEY_V,
            KeyCode.B               to KEY_B,
            KeyCode.N               to KEY_N,
            KeyCode.M               to KEY_M,
            KeyCode.COMMA           to KEY_COMMA,
            KeyCode.PERIOD          to KEY_PERIOD,
            KeyCode.SLASH           to KEY_SLASH,
            KeyCode.MULTIPLY        to KEY_MULTIPLY,
            KeyCode.SPACE           to KEY_SPACE,
            KeyCode.CAPS            to KEY_CAPITAL,
            KeyCode.F1              to KEY_F1,
            KeyCode.F2              to KEY_F2,
            KeyCode.F3              to KEY_F3,
            KeyCode.F4              to KEY_F4,
            KeyCode.F5              to KEY_F5,
            KeyCode.F6              to KEY_F6,
            KeyCode.F7              to KEY_F7,
            KeyCode.F8              to KEY_F8,
            KeyCode.F9              to KEY_F9,
            KeyCode.F10             to KEY_F10,
            KeyCode.NUM_LOCK        to KEY_NUMLOCK,
            KeyCode.SCROLL_LOCK     to KEY_SCROLL,
            KeyCode.NUMPAD7         to KEY_NUMPAD7,
            KeyCode.NUMPAD8         to KEY_NUMPAD8,
            KeyCode.NUMPAD9         to KEY_NUMPAD9,
            KeyCode.SUBTRACT        to KEY_SUBTRACT,
            KeyCode.NUMPAD4         to KEY_NUMPAD4,
            KeyCode.NUMPAD5         to KEY_NUMPAD5,
            KeyCode.NUMPAD6         to KEY_NUMPAD6,
            KeyCode.ADD             to KEY_ADD,
            KeyCode.NUMPAD1         to KEY_NUMPAD1,
            KeyCode.NUMPAD2         to KEY_NUMPAD2,
            KeyCode.NUMPAD3         to KEY_NUMPAD3,
            KeyCode.NUMPAD0         to KEY_NUMPAD0,
            KeyCode.DECIMAL         to KEY_DECIMAL,
            KeyCode.F11             to KEY_F11,
            KeyCode.F12             to KEY_F12,
            KeyCode.F13             to KEY_F13,
            KeyCode.F14             to KEY_F14,
            KeyCode.F15             to KEY_F15,
            KeyCode.KANA            to KEY_KANA,
            KeyCode.CONVERT         to KEY_CONVERT,
            KeyCode.NONCONVERT      to KEY_NOCONVERT,
            KeyCode.CIRCUMFLEX      to KEY_CIRCUMFLEX,
            KeyCode.AT              to KEY_AT,
            KeyCode.COLON           to KEY_COLON,
            KeyCode.UNDERSCORE      to KEY_UNDERLINE,
            KeyCode.STOP            to KEY_STOP,
            KeyCode.DIVIDE          to KEY_DIVIDE,
            KeyCode.PAUSE           to KEY_PAUSE,
            KeyCode.HOME            to KEY_HOME,
            KeyCode.UP              to KEY_UP,
            KeyCode.PAGE_UP         to KEY_PRIOR,
            KeyCode.LEFT            to KEY_LEFT,
            KeyCode.RIGHT           to KEY_RIGHT,
            KeyCode.END             to KEY_END,
            KeyCode.DOWN            to KEY_DOWN,
            KeyCode.PAGE_DOWN       to KEY_NEXT,
            KeyCode.INSERT          to KEY_INSERT,
            KeyCode.DELETE          to KEY_DELETE,
            KeyCode.ALT             to KEY_LMENU,
            KeyCode.META            to KEY_RCONTROL,
        )

        private val JME_TO_KEY_CODE = KEY_CODE_TO_JME.map {
            it.value to it.key
        }.toMap()
    }

    private val keyInputEvents = LinkedList<KeyInputEvent>()

    private val processKeyPressed = { event: KeyEvent ->
        onKeyEvent(event, true)
    }

    private val processKeyReleased = { event: KeyEvent ->
        onKeyEvent(event, false)
    }


    override fun bind(node: Node) {
        super.bind(node)
        node.addEventHandler(KeyEvent.KEY_PRESSED, processKeyPressed)
        node.addEventHandler(KeyEvent.KEY_RELEASED, processKeyReleased)
    }

    override fun unbind() {
        super.unbind()
        node?.removeEventHandler(KeyEvent.KEY_PRESSED, processKeyPressed)
        node?.removeEventHandler(KeyEvent.KEY_RELEASED, processKeyReleased)
    }

    private fun onKeyEvent(keyEvent: KeyEvent, pressed: Boolean) {
        val code = KEY_CODE_TO_JME.getOrElse(keyEvent.code) { KEY_UNKNOWN }
        val character = keyEvent.text!!
        val keyChar = if(character.isEmpty()) 0.toChar() else character[0].toChar()

        val event = KeyInputEvent(code, keyChar, pressed, false)
        event.time = inputTimeNanos

        keyInputEvents.add(event)
    }

    override fun update() {
        while (!keyInputEvents.isEmpty()) {
            listener?.onKeyEvent(keyInputEvents.poll())
        }
    }

    override fun getKeyName(key: Int): String? =
        JME_TO_KEY_CODE[key]?.name ?: ""

}