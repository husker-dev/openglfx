package com.huskerdev.openglfx.libgdx.test.game

import com.huskerdev.openglfx.canvas.GLCanvasAnimator
import com.huskerdev.openglfx.libgdx.LibGDXCanvas
import com.huskerdev.openglfx.libgdx.test.game.game.MyGdxGame
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Stage

fun main(){
    Application.launch(GameApplication::class.java)
}

class GameApplication: Application() {
    override fun start(stage: Stage) {
        stage.width = 800.0
        stage.height = 700.0
        stage.scene = createScene()
        stage.show()
    }

    fun createScene(): Scene{
        val canvas = LibGDXCanvas(MyGdxGame())
        canvas.minWidth = 300.0
        canvas.minHeight = 300.0
        canvas.animator = GLCanvasAnimator(60.0)

        return Scene(BorderPane(canvas, HBox(Button("test"), Button("test1")), null, null, null))
    }
}