package com.huskerdev.openglfx.libgdx.test.game.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.huskerdev.openglfx.libgdx.internal.OGLFXApplication

class MyGdxGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var img: Texture

    var mouseX = 0
    var mouseY = 0

    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")

        Gdx.input.inputProcessor = object: InputProcessor {
            override fun keyDown(keycode: Int): Boolean {
                println("key down ($keycode)")
                return false
            }

            override fun keyUp(keycode: Int): Boolean {
                println("key up ($keycode)")
                return false
            }

            override fun keyTyped(character: Char): Boolean {
                println("key typed ($character)")
                return false
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                println("touch down (screenX:$screenX, screenY:$screenY, pointer:$pointer, button:$button)")
                return false
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                println("touch up (screenX:$screenX, screenY:$screenY, pointer:$pointer, button:$button)")
                return false
            }

            override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                println("touch cancelled (screenX:$screenX, screenY:$screenY, pointer:$pointer, button:$button)")
                return false
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                println("touch dragged (screenX:$screenX, screenY:$screenY, pointer:$pointer)")
                return false
            }

            override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
                println("touch moved (screenX:$screenX, screenY:$screenY)")
                mouseX = screenX
                mouseY = screenY
                return false
            }

            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                println("scrolled (amountX:$amountX, amountY:$amountY)")
                return false
            }
        }
    }

    override fun render() {
        if((Gdx.app as OGLFXApplication).canvas.isFocused)
            ScreenUtils.clear(0f, 1f, 0f, 1f)
        else
            ScreenUtils.clear(1f, 0f, 0f, 1f)
        batch.begin()
        batch.draw(img, mouseX.toFloat(), mouseY.toFloat())
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}
