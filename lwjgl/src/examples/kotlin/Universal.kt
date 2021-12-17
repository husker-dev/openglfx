import com.husker.openglfx.DirectDrawPolicy
import com.husker.openglfx.OpenGLCanvas
import com.husker.openglfx.lwjgl.LWJGL_MODULE
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.lwjgl.opengl.GL30.*

fun main(){
    System.setProperty("prism.vsync", "false")
    Application.launch(UniversalExampleApp::class.java)
}

class UniversalExampleApp: Application(){
    override fun start(stage: Stage?) {
        if(stage == null)
            return

        stage.width = 300.0
        stage.height = 300.0
        stage.scene = Scene(createGL())

        stage.show()
    }

    private fun createGL(): Region {
        val canvas = OpenGLCanvas.create(LWJGL_MODULE, DirectDrawPolicy.NEVER)
        canvas.onReshape{
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(0.0, canvas.scene.width, 0.0, canvas.scene.height, -1.0, 100.0)
        }
        canvas.onNGRender{
            glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            glClearDepth(1.0)

            glColor3f(1.0f, 0.5f, 0.0f)
            glBegin(GL_QUADS)
            glVertex3d(0.0, 0.0, 0.0)
            glVertex3d(200.0, 0.0, 0.0)
            glVertex3d(200.0, 200.0, 0.0)
            glVertex3d(0.0, 200.0, 0.0)
            glEnd()
        }

        return canvas
    }
}