// https://learnopengl.com/Getting-started/Textures

import com.huskerdev.openglfx.GLCanvasAnimator
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.lwjgl.opengl.GL11

fun main() {
    Application.launch(WhiteNotBlackExampleApp::class.java)
}

class WhiteNotBlackExampleApp : Application() {

    override fun start(stage: Stage) {
        stage.title = "WhiteNotBlack"
        stage.width = 400.0
        stage.height = 400.0
        stage.scene = Scene(createGL())
        stage.show()
    }

    private fun createGL(): Region {
        val canvas = OpenGLCanvas.create(LWJGLExecutor.LWJGL_MODULE)
        canvas.animator = GLCanvasAnimator(60.0)
        canvas.addOnReshapeEvent(::reshape)
        canvas.addOnRenderEvent(::render)
        return canvas
    }

    fun reshape(event: GLReshapeEvent) {
        val width: Int = event.width
        val height: Int = event.height
        GL11.glViewport(0, 0, width, height)
    }

    fun render(event: GLRenderEvent) {
        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
    }

}
