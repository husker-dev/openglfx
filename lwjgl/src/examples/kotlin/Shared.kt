import com.huskerdev.openglfx.GLCanvasAnimator
import com.huskerdev.openglfx.GLCanvasAnimator.Companion.UNLIMITED_FPS
import com.huskerdev.openglfx.lwjgl.shared.LWJGLShared
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.lwjgl.opengl.GL30.*
import rendering.ExampleRenderer
import kotlin.math.sin

fun main(){
    System.setProperty("prism.order", "es2,d3d,sw")
    System.setProperty("prism.vsync", "false")
    Application.launch(SharedExampleApp::class.java)
}

class SharedExampleApp: Application(){

    private lateinit var stage: Stage

    override fun start(stage: Stage?) {
        this.stage = stage!!

        stage.width = 300.0
        stage.height = 300.0
        stage.scene = Scene(createGL())

        stage.show()
    }

    private fun createGL(): Region {
        val canvas = LWJGLShared()
        canvas.animator = GLCanvasAnimator(60.0, started = true)

        canvas.onReshape { ExampleRenderer.reshape(canvas, it) }
        canvas.onRender { ExampleRenderer.render(canvas, it) }

        return canvas
    }
}