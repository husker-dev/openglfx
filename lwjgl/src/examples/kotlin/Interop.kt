
import com.huskerdev.openglfx.GLCanvasAnimator
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor.Companion.LWJGL_MODULE
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.scene.layout.Region
import javafx.stage.Stage
import rendering.ExampleRenderer

fun main() {
    System.setProperty("prism.order", "d3d,sw")
    System.setProperty("prism.vsync", "false")

    Application.launch(InteropExampleApp::class.java)
}

class InteropExampleApp: Application(){

    override fun start(stage: Stage?) {
        stage!!.title = "Kotlin \"Interop\" example"
        stage.width = 400.0
        stage.height = 400.0

        stage.scene = Scene(SplitPane(createGL(), createGL()))
        stage.show()
    }

    private fun createGL(): Region {
        val canvas = OpenGLCanvas.create(LWJGL_MODULE)
        canvas.animator = GLCanvasAnimator(60.0)

        canvas.addOnReshapeEvent(ExampleRenderer::reshape)
        canvas.addOnRenderEvent(ExampleRenderer::render)

        return canvas
    }
}