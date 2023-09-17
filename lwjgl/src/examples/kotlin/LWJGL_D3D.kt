
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.OpenGLCanvasAnimator
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor.Companion.LWJGL_MODULE
import com.huskerdev.openglfx.renderdoc.RenderDoc
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import scene.ExampleScene

fun main() {
    RenderDoc.enabled = true
    System.setProperty("prism.order", "d3d")
    System.setProperty("prism.vsync", "false")

    Application.launch(D3DExampleApp::class.java)
}

class D3DExampleApp: Application(){

    override fun start(stage: Stage?) {
        stage!!.title = "OpenGLCanvas D3D example"
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
        val canvas = OpenGLCanvas.create(LWJGL_MODULE)
        canvas.animator = OpenGLCanvasAnimator(60.0)

        val renderExample = ExampleScene()
        canvas.addOnInitEvent(renderExample::init)
        canvas.addOnReshapeEvent(renderExample::reshape)
        canvas.addOnRenderEvent(renderExample::render)

        return canvas
    }
}