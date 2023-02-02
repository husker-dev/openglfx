// https://learnopengl.com/Getting-started/Textures

import com.huskerdev.openglfx.GLCanvasAnimator
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.events.GLInitializeEvent
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor
import de.matthiasmann.twl.utils.PNGDecoder
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.lwjgl.BufferUtils.createByteBuffer
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_STATIC_DRAW
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

fun main() {
    System.setProperty("prism.order", "es2")
    Application.launch(TexturesExampleApp::class.java)
}

class TexturesExampleApp : Application() {

    override fun start(stage: Stage) {
        stage.title = "Learn Opengl - Textures"
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

    var textureId: Int = 0
    var bufferId: Int = 0
    var programId: Int = 0
    val bufferData: FloatBuffer = ByteBuffer.allocateDirect(100 shl 2).order(ByteOrder.nativeOrder()).asFloatBuffer()
    var isDirty: Boolean = true

    fun init(event: GLInitializeEvent) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        val vertexShader: String = """
            |#version 330 core
            |
            |layout (location = 0) in vec3 aPos;
            |layout (location = 1) in vec2 aTexCoord;
            |
            |out vec2 TexCoord;
            |
            |void main()
            |{
            |	gl_Position = vec4(aPos, 1.0);
            |	TexCoord = vec2(aTexCoord.x, aTexCoord.y);
            |}         
            |""".trimMargin("|")
        val fragmentShader: String = """  
            |#version 330 core
            |
            |out vec4 FragColor;
            |
            |in vec2 TexCoord;
            |
            |uniform sampler2D texture1;
            |
            |void main()
            |{
            |	FragColor = texture(texture1, TexCoord);
            |}
            |""".trimMargin("|")

        // 1. load texture
        textureId = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        val inputStream: InputStream? = ClassLoader.getSystemResourceAsStream("awesomeface.png")
        if (inputStream == null) {
            error("image not loaded")
        }
        val pngDecoder: PNGDecoder = PNGDecoder(inputStream)
        val width: Int = pngDecoder.width
        val height: Int = pngDecoder.height
        val alignment: Int = 1
        val stride: Int = 4 * alignment * width
        val buffer: ByteBuffer = createByteBuffer(stride * height)
        pngDecoder.decodeFlipped(buffer, stride, PNGDecoder.Format.RGBA) // IMPORTANT flip
        buffer.flip()
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, alignment)
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)
        inputStream.close()
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

        // compile program
        programId = GL20.glCreateProgram()
        val vertexShaderId: Int = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertexShaderId, vertexShader)
        GL20.glCompileShader(vertexShaderId)
        if (GL20.glGetShaderi(vertexShaderId, GL20.GL_COMPILE_STATUS) == GL_FALSE) {
            error("vertex shader not compiled")
        }
        val pixelShaderId: Int = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(pixelShaderId, fragmentShader)
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

        // generate buffer
        bufferId = GL15.glGenBuffers()
        bufferData.put(
            floatArrayOf(
                // positions        // texture coords
                0.5f, 0.5f, 0.0f, 1.0f, 1.0f, // top right
                0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
                -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
                0.5f, 0.5f, 0.0f, 1.0f, 1.0f, // top right
                -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
                -0.5f, 0.5f, 0.0f, 0.0f, 1.0f  // top left
            )
        )
        bufferData.rewind()
    }

    fun reshape(event: GLReshapeEvent) {
        val width: Int = event.width
        val height: Int = event.height
        GL11.glViewport(0, 0, width, height)
    }

    fun render(event: GLRenderEvent) {
        GL11.glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        GL20.glUseProgram(programId)
        GL15.glBindBuffer(GL_ARRAY_BUFFER, bufferId)
        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * 4, 0)
        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * 4, 3 * 4)
        if (isDirty) {
            GL15.glBufferData(GL_ARRAY_BUFFER, bufferData, GL_STATIC_DRAW)
            isDirty = false
        }
        GL20.glActiveTexture(GL13.GL_TEXTURE0)
        GL20.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        GL20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        GL20.glUniform1i(GL20.glGetUniformLocation(programId, "texture1"), 0)
        GL11.glDrawArrays(GL_TRIANGLES, 0, 6)
    }

}
