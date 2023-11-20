package com.huskerdev.openglfx.jogl.example
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLCanvasAnimator
import com.huskerdev.openglfx.jogl.JOGL_MODULE
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import com.huskerdev.openglfx.jogl.example.scene.ExampleScene


fun main(){
    System.setProperty("prism.order", "sw")
    System.setProperty("prism.vsync", "false")

    Application.launch(SWExampleApp::class.java)
}

class SWExampleApp: Application(){

    override fun start(stage: Stage?) {
        stage!!.title = "OpenGLCanvas SW example"
        stage.width = 800.0
        stage.height = 600.0

        stage.scene = Scene(StackPane(createUILayer(), createGL()))
        stage.show()
    }

    private fun createUILayer() = object: Pane(){
        init {
            children.add(Label("OpenGLCanvas is not opaque, so you can see this text"))
        }
    }

    private fun createGL(): Region {
        val canvas = GLCanvas.create(JOGL_MODULE)
        canvas.animator = GLCanvasAnimator(60.0)

        val renderExample = ExampleScene()
        canvas.addOnInitEvent(renderExample::init)
        canvas.addOnReshapeEvent(renderExample::reshape)
        canvas.addOnRenderEvent(renderExample::render)

        return canvas
    }
}