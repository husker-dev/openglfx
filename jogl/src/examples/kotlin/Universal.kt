import com.huskerdev.openglfx.DirectDrawPolicy
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.jogl.JOGLFXCanvas
import com.huskerdev.openglfx.jogl.JOGL_MODULE
import com.jogamp.opengl.GL2ES3.GL_QUADS
import com.jogamp.opengl.fixedfunc.GLMatrixFunc
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import kotlin.math.sin


fun main(){
    System.setProperty("prism.vsync", "false")
    Application.launch(UniversalExampleApp::class.java)
}

class UniversalExampleApp: Application(){

    private lateinit var stage: Stage

    override fun start(stage: Stage?) {
        this.stage = stage!!

        stage.width = 300.0
        stage.height = 300.0
        stage.scene = Scene(createGL())

        stage.show()
    }

    private fun createGL(): Region{
        val canvas = OpenGLCanvas.create(JOGL_MODULE, DirectDrawPolicy.NEVER)
        canvas.createTimer(200.0)

        var animVar = 0.0
        var y = 0.0

        canvas.onReshape{
            val gl = (canvas as JOGLFXCanvas).gl

            gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
            gl.glLoadIdentity()
            gl.glOrtho(0.0, canvas.scene.width, 0.0, canvas.scene.height, -1.0, 100.0)
        }
        canvas.onUpdate {
            animVar += 0.1
            y = sin(animVar) * (stage.height / 3)
        }
        canvas.onRender{
            val gl = (canvas as JOGLFXCanvas).gl
            val width = stage.width
            val height = stage.height

            gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            gl.glClearDepth(1.0)

            gl.glColor3f(1.0f, 0.5f, 0.0f)
            gl.glBegin(GL_QUADS)
            gl.glVertex2d(0.0, 0.0)
            gl.glVertex2d(width, 0.0)
            gl.glVertex2d(width, height)
            gl.glVertex2d(0.0, height)
            gl.glEnd()

            // Moving rectangle
            val rectSize = 40.0
            val rectX = (width - rectSize) / 2
            val rectY = (height - rectSize) / 2 + y

            gl.glColor3f(0f, 0.5f, 0.0f)
            gl.glBegin(GL_QUADS)
            gl.glVertex2d(rectX, rectY)
            gl.glVertex2d(rectX + rectSize, rectY)
            gl.glVertex2d(rectX + rectSize, rectY + rectSize)
            gl.glVertex2d(rectX, rectY + rectSize)
            gl.glEnd()
        }

        return canvas
    }

}