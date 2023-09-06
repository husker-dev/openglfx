import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.OpenGLCanvasAnimator
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor.Companion.LWJGL_MODULE
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.scene.layout.Region
import javafx.stage.Stage

fun main(){
    System.setProperty("prism.order", "es2")
    System.setProperty("prism.vsync", "false")

    Application.launch(SharedExampleApp::class.java)
}

class SharedExampleApp: Application(){

    override fun start(stage: Stage?) {
        stage!!.title = "Kotlin \"ES2 pipeline\" example"
        stage.width = 400.0
        stage.height = 400.0

        stage.scene = Scene(SplitPane(createGL(), createGL()))
        stage.show()
    }

    private fun createGL(): Region {
        val canvas = OpenGLCanvas.create(LWJGL_MODULE)
        canvas.animator = OpenGLCanvasAnimator(60.0)

        canvas.addOnReshapeEvent(ExampleRenderer::reshape)
        canvas.addOnRenderEvent(ExampleRenderer::render)

        return canvas
    }
}