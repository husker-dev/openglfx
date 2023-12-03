import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLCanvasAnimator
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor.Companion.LWJGL_MODULE
import com.sun.javafx.scene.layout.RegionHelper
import com.sun.prism.GraphicsPipeline
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.layout.Region.USE_PREF_SIZE
import javafx.scene.paint.Paint
import javafx.stage.Stage
import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL30.GL_MAJOR_VERSION
import org.lwjgl.opengl.GL30.GL_MINOR_VERSION
import scene.ExampleScene


fun main() {
    System.setProperty("prism.vsync", "false")
    Application.launch(ExampleApp::class.java)
}

class ExampleApp: Application(){

    private lateinit var stage: Stage
    private lateinit var canvas: GLCanvas
    private var majorVersion = -1
    private var minorVersion = -1
    private var iteration = 0

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
        iteration++
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
                if(it.code == KeyCode.F4) canvas.fxaa = !canvas.fxaa
                if(it.code == KeyCode.F5) canvas.msaa = if(canvas.msaa > 0) 0 else 8
                if(it.code == KeyCode.F6) canvas.flipY = !canvas.flipY
            }
        }
    }

    private fun createGLCanvasInstance(): GLCanvas {
        val canvas = GLCanvas(LWJGL_MODULE, msaa = 0, fxaa = true, profile = GLProfile.Core, async = true, interopType = GLInteropType.NVDXInterop)
        canvas.animator = GLCanvasAnimator(60.0)

        val renderExample = ExampleScene()

        canvas.addOnInitEvent(renderExample::init)
        canvas.addOnReshapeEvent(renderExample::reshape)
        canvas.addOnRenderEvent(renderExample::render)

        return canvas
    }

    private fun createSampleText() = Label().apply{
        text = "GLCanvas is not opaque, so you can see this text"
        StackPane.setAlignment(this, Pos.TOP_RIGHT)
    }

    private fun createDebugPanel(canvas: GLCanvas) = VBox().apply{
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE)
        padding = Insets(5.0, 5.0, 5.0, 5.0)
        StackPane.setAlignment(this, Pos.TOP_LEFT)
        background = Background(BackgroundFill(Paint.valueOf("#ffffffaa"), CornerRadii.EMPTY, Insets.EMPTY))
        val separator = "separator" to {}

        val params = arrayListOf<Pair<String, () -> Any>>(
            "Invoke GC" to { "F1" },
            "Recreate canvas" to { "F2" },
            "Add canvas" to { "F3" },
            "Toggle FXAA" to { "F4" },
            "Toggle MSAA x8" to { "F5" },
            "Toggle Flip-Y" to { "F6" },
            separator,
            "FPS" to {
                "${canvas.fpsCounter.currentFps}/${(1000 / (canvas.fpsCounter.delta * 1000)).toInt()}"
            },
            "Size" to {
                "${canvas.scaledWidth} x ${canvas.scaledHeight}"
            },
            "DPI" to { canvas.dpi.toString() },
            separator,
            "Pipeline" to { GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3].uppercase() },
            "GLInteropType" to { canvas.interopType },
            "Impl" to { RegionHelper.getPeer<NGGLCanvas>(canvas)::class.java.simpleName },
            separator,
            "GLProfile" to { "${canvas.profile} ${majorVersion}.${minorVersion}" },
            "Async" to { canvas.async },
            "Flip-Y" to { canvas.flipY },
            "MSAA" to { canvas.msaa },
            "FXAA" to { canvas.fxaa },
            separator,
            "JVM MEMORY USAGE" to {
                "${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024} Mb"
            },
            "ITERATION" to { iteration }
        )

        val labels = hashMapOf<Label, () -> Any>()
        params.forEach {
            if(it.first == "separator")
                children.add(Separator())
            else {
                children.add(BorderPane().apply {
                    minWidth = 220.0
                    left = Label(it.first + ":")
                    right = Label(it.second().toString())
                    labels[right as Label] = it.second
                })
            }
        }
        canvas.addOnRenderEvent { e ->
            if(majorVersion == -1)
                majorVersion = glGetInteger(GL_MAJOR_VERSION)
            if(minorVersion == -1)
                minorVersion = glGetInteger(GL_MINOR_VERSION)
            Platform.runLater {
                labels.forEach {
                    it.key.text = it.value().toString()
                }
            }
        }
    }
}