
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLCanvasAnimator
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.jogl.JOGL_MODULE
import com.huskerdev.openglfx.jogl.example.scene.ExampleScene
import com.sun.prism.GraphicsPipeline
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.stage.Stage

fun main() {
    System.setProperty("prism.vsync", "false")
    Application.launch(ExampleApp::class.java)
}

class ExampleApp: Application(){

    override fun start(stage: Stage) {
        stage.title = "GLCanvas example"
        stage.width = 800.0
        stage.height = 600.0

        val glCanvas = createGLCanvas()

        stage.scene = Scene(StackPane(createDebugPanel(glCanvas), glCanvas))
        stage.show()
    }

    private fun createGLCanvas(): GLCanvas {
        val canvas = GLCanvas.create(JOGL_MODULE, msaa = 4, profile = GLProfile.Core, async = true)
        canvas.animator = GLCanvasAnimator(60.0)

        val renderExample = ExampleScene()
        canvas.addOnInitEvent(renderExample::init)
        canvas.addOnReshapeEvent(renderExample::reshape)
        canvas.addOnRenderEvent(renderExample::render)

        return canvas
    }

    private fun createDebugPanel(canvas: GLCanvas) = VBox().apply{
        children.add(Label("OpenGLCanvas is not opaque, so you can see this text"))
        children.add(Label("----------------------------------------"))
        arrayListOf(
            "PIPELINE" to GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3],
            "INTEROP" to canvas.interopType,
            "IMPL" to canvas::class.java.simpleName,
            "MSAA" to canvas.msaa,
            "PROFILE" to canvas.profile,
            "FLIP_Y" to canvas.flipY,
            "IS_ASYNC" to canvas.isAsync,
            "FPS" to "-",
            "SIZE" to "0x0"
        ).forEach {
            children.add(BorderPane().apply {
                maxWidth = 190.0
                left = Label(it.first + ":")
                right = Label(it.second.toString()).apply { id = it.first }
            })
        }
        canvas.addOnRenderEvent { e ->
            Platform.runLater {
                (scene.lookup("#FPS") as Label).text = "${e.fps}/${(1000 / (e.delta * 1000)).toInt()}"
                (scene.lookup("#SIZE") as Label).text = "${e.width}x${e.height}"
            }
        }
    }
}