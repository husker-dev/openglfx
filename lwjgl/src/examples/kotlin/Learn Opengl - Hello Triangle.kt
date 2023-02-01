// https://learnopengl.com/Getting-started/Hello-Triangle

import com.huskerdev.openglfx.GLCanvasAnimator
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.events.GLInitializeEvent
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_STATIC_DRAW
import org.lwjgl.opengl.GL20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun main() {
    System.setProperty("prism.order", "d3d,sw")
    System.setProperty("prism.vsync", "false")

    Application.launch(HelloTriangleExampleApp::class.java)
}

class HelloTriangleExampleApp : Application() {

    override fun start(stage: Stage) {
        stage.title = "Learn Opengl - Hello Triangle"
        stage.width = 400.0
        stage.height = 400.0
        stage.scene = Scene(createGL())
        stage.show()
    }

    private fun createGL(): Region {
        val canvas = OpenGLCanvas.create(LWJGLExecutor.LWJGL_MODULE)
        canvas.animator = GLCanvasAnimator(60.0)
        canvas.addOnInitEvent(::init)
        canvas.addOnReshapeEvent(::reshape)
        canvas.addOnRenderEvent(::render)
        return canvas
    }

    var bufferId: Int = 0
    var programId: Int = 0
    val bufferData: FloatBuffer = ByteBuffer.allocateDirect(100 shl 2).order(ByteOrder.nativeOrder()).asFloatBuffer()

    fun init(event: GLInitializeEvent) {
        val vertexShaderSource: String = """
            attribute vec3 position;
            
            void main() {
                gl_Position = vec4(position, 1);
            }
        """.trimIndent()
        val pixelShaderSource: String = """            
            |void main() {
            |    gl_Color = vec4(1.0, 0.5, 0.2, 1.0);
            |}
        """.trimMargin("|")

        programId = GL20.glCreateProgram()

        val vertexShaderId: Int = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertexShaderId, vertexShaderSource)
        GL20.glCompileShader(vertexShaderId)
        if (GL20.glGetShaderi(vertexShaderId, GL20.GL_COMPILE_STATUS) == GL_FALSE) {
            error("vertex shader not compiled")
        }

        val pixelShaderId: Int = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(pixelShaderId, pixelShaderSource)
        GL20.glCompileShader(pixelShaderId)
        if (GL20.glGetShaderi(pixelShaderId, GL20.GL_COMPILE_STATUS) == GL_FALSE) {
            error("pixel shader not compiled")
        }

        GL20.glAttachShader(programId, vertexShaderId)
        GL20.glAttachShader(programId, pixelShaderId)
        GL20.glLinkProgram(programId)

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL_FALSE) {
            error("program not linked")
        }

        GL20.glDeleteShader(vertexShaderId)
        GL20.glDeleteShader(pixelShaderId)

        bufferData.put(floatArrayOf(-0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.0f, 0.5f, 0.0f))
        bufferId = GL15.glGenBuffers()
    }

    fun reshape(event: GLReshapeEvent) {
        val width: Int = event.width
        val height: Int = event.height
        GL11.glViewport(0, 0, width, height)
    }

    fun render(event: GLRenderEvent) {
        GL20.glUseProgram(programId)
        GL15.glBindBuffer(GL_ARRAY_BUFFER, bufferId)
        GL15.glBufferData(GL_ARRAY_BUFFER, bufferData, GL_STATIC_DRAW)
        GL11.glDrawArrays(GL_TRIANGLES, 0, 3)
    }

}
