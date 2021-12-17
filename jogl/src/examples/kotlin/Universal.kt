import com.husker.openglfx.DirectDrawPolicy
import com.husker.openglfx.OpenGLCanvas
import com.husker.openglfx.jogl.JOGLFXCanvas
import com.husker.openglfx.jogl.JOGL_MODULE
import com.jogamp.opengl.GL2ES3.GL_QUADS
import com.jogamp.opengl.fixedfunc.GLMatrixFunc
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage


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

    fun createGL(): Region{
        val canvas = OpenGLCanvas.create(JOGL_MODULE, DirectDrawPolicy.NEVER)
        canvas.onReshape{
            val gl = (canvas as JOGLFXCanvas).gl

            gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
            gl.glLoadIdentity()
            gl.glOrtho(0.0, canvas.scene.width, 0.0, canvas.scene.height, -1.0, 100.0)
        }
        canvas.onNGRender{
            val gl = (canvas as JOGLFXCanvas).gl
            gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            gl.glClearDepth(1.0)

            gl.glColor3f(1.0f, 0.5f, 0.0f)
            gl.glBegin(GL_QUADS)
            gl.glVertex3d(0.0, 0.0, 0.0)
            gl.glVertex3d(200.0, 0.0, 0.0)
            gl.glVertex3d(200.0, 200.0, 0.0)
            gl.glVertex3d(0.0, 200.0, 0.0)
            gl.glEnd()
        }

        return canvas
    }

}