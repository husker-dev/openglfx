package scene.graphics

import org.lwjgl.opengl.GL20.*

class Shader(vertexSource: String, fragmentSource: String) {
    val program: Int

    init {
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, vertexSource)
        glCompileShader(vertexShader)

        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fragmentSource)
        glCompileShader(fragmentShader)

        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
    }
}