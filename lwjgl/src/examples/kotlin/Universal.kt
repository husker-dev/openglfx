import com.huskerdev.openglfx.DirectDrawPolicy
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.lwjgl.LWJGL_MODULE
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.lwjgl.opengl.GL30.*
import kotlin.math.sin

fun main(){
    System.setProperty("prism.vsync", "false")
    Application.launch(UniversalExampleApp::class.java)
}

class UniversalExampleApp: Application(){

    lateinit var stage: Stage

    override fun start(stage: Stage?) {
        this.stage = stage!!

        stage.width = 300.0
        stage.height = 300.0
        stage.scene = Scene(createGL())

        stage.show()
    }

    private fun createGL(): Region {
        val canvas = OpenGLCanvas.create(LWJGL_MODULE, DirectDrawPolicy.NEVER)
        canvas.createTimer(200.0)

        var animVar = 0.0
        var y = 0.0

        canvas.onReshape {
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(0.0, canvas.scene.width, 0.0, canvas.scene.height, -1.0, 100.0)
        }
        canvas.onRender {
            animVar += 0.1
            y = sin(animVar) * (stage.height / 3)

            val width = stage.width
            val height = stage.height

            glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            glClearDepth(1.0)

            glColor3f(1.0f, 0.5f, 0.0f)
            glBegin(GL_QUADS)
            glVertex2d(0.0, 0.0)
            glVertex2d(width, 0.0)
            glVertex2d(width, height)
            glVertex2d(0.0, height)
            glEnd()

            // Moving rectangle
            val rectSize = 40.0
            val rectX = (width - rectSize) / 2
            val rectY = (height - rectSize) / 2 + y

            glColor3f(0f, 0.5f, 0.0f)
            glBegin(GL_QUADS)
            glVertex2d(rectX, rectY)
            glVertex2d(rectX + rectSize, rectY)
            glVertex2d(rectX + rectSize, rectY + rectSize)
            glVertex2d(rectX, rectY + rectSize)
            glEnd()
        }

        return canvas
    }
}