import com.huskerdev.openglfx.DirectDrawPolicy
import com.huskerdev.openglfx.GLCanvasAnimator
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.jogl.JOGL_MODULE
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import rendering.ExampleRenderer


fun main(){
    System.setProperty("prism.order", "es2,d3d,sw")
    System.setProperty("prism.vsync", "false")
    Application.launch(DirectExampleApp::class.java)
}

class DirectExampleApp: Application(){

    private lateinit var stage: Stage

    override fun start(stage: Stage?) {
        this.stage = stage!!

        stage.width = 300.0
        stage.height = 300.0
        stage.scene = Scene(createGL())

        stage.show()
    }

    private fun createGL(): Region{
        val canvas = OpenGLCanvas.create(JOGL_MODULE, DirectDrawPolicy.ALWAYS)
        canvas.animator = GLCanvasAnimator(60.0, started = true)

        canvas.onReshape { ExampleRenderer.reshape(canvas, it) }
        canvas.onRender { ExampleRenderer.render(canvas, it) }

        return canvas
    }
}