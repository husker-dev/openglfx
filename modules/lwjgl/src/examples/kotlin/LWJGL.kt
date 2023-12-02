import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLCanvasAnimator
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor.Companion.LWJGL_MODULE
import com.sun.javafx.scene.layout.RegionHelper
import com.sun.prism.GraphicsPipeline
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import scene.ExampleScene


fun main() {
    System.setProperty("prism.vsync", "false")
    Application.launch(ExampleApp::class.java)
}

class ExampleApp: Application(){

    private lateinit var stage: Stage
    private lateinit var canvas: GLCanvas
    private var iteration = 1

    override fun start(stage: Stage) {
        this.stage = stage
        stage.title = "GLCanvas example"
        stage.width = 800.0
        stage.height = 600.0
        recreateGLCanvas()
        stage.show()
        stage.toFront()
    }

    private fun recreateGLCanvas(){
       if(::canvas.isInitialized)
           canvas.dispose()
        canvas = createGLCanvasInstance()
        val splitPane = SplitPane(canvas).apply {
            style = """
                    .split-pane-divider {
                        -fx-border-color: transparent transparent transparent transparent;
                        -fx-background-color: transparent, transparent;
                        -fx-background-insets: 0, 0 1 0 1;
                    }
                    """.trimIndent()
        }
        val sampleText = createSampleText()
        val debugPane = createDebugPanel(canvas)

        stage.scene = Scene(StackPane(sampleText, splitPane, debugPane)).apply {
            onKeyPressed = EventHandler {
                if(it.code == KeyCode.F1) System.gc()
                if(it.code == KeyCode.F2) recreateGLCanvas()
                if(it.code == KeyCode.F3) splitPane.items.add(createGLCanvasInstance())
            }
        }
        iteration++
    }

    private fun createGLCanvasInstance(): GLCanvas {
        val canvas = GLCanvas(LWJGL_MODULE, msaa = 4, profile = GLProfile.Core, async = true)
        canvas.animator = GLCanvasAnimator(60.0)

        val renderExample = ExampleScene()

        canvas.addOnInitEvent(renderExample::init)
        canvas.addOnReshapeEvent(renderExample::reshape)
        canvas.addOnRenderEvent(renderExample::render)

        return canvas
    }

    private fun createSampleText() = Label().apply{
        text = "OpenGLCanvas is not opaque, so you can see this text"
        StackPane.setAlignment(this, Pos.TOP_RIGHT)
    }

    private fun createDebugPanel(canvas: GLCanvas) = VBox().apply{
        children.add(Label("Press F1 to invoke GC"))
        children.add(Label("Press F2 to recreate canvas"))
        children.add(Label("Press F3 to add canvas"))
        children.add(Label("----------------------------------------"))
        arrayListOf(
            "PIPELINE" to GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3],
            "INTEROP" to canvas.interopType,
            "IMPL" to RegionHelper.getPeer<NGGLCanvas>(canvas)::class.java.simpleName,
            "MSAA" to canvas.msaa,
            "PROFILE" to canvas.profile,
            "FLIP_Y" to canvas.flipY,
            "IS_ASYNC" to canvas.async,
            "FPS" to "-",
            "SIZE" to "0x0",
            "DPI" to "",
            "MEMORY_USAGE" to "0",
            "ITERATION" to iteration
        ).forEach {
            children.add(BorderPane().apply {
                maxWidth = 220.0
                left = Label(it.first + ":")
                right = Label(it.second.toString()).apply { id = it.first }
            })
        }
        canvas.addOnRenderEvent { e ->
            Platform.runLater {
                (scene.lookup("#FPS") as Label).text = "${e.fps}/${(1000 / (e.delta * 1000)).toInt()}"
                (scene.lookup("#SIZE") as Label).text = "${e.width}x${e.height}"
                (scene.lookup("#DPI") as Label).text = canvas.dpi.toString()
                (scene.lookup("#MEMORY_USAGE") as Label).text =
                    "${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024} Mb"
            }
        }
    }
}