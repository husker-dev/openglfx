package scene.graphics

import org.lwjgl.opengl.GL30

class Shader(vertexSource: String, fragmentSource: String) {
    val program: Int

    init {
        val vertexShader = GL30.glCreateShader(GL30.GL_VERTEX_SHADER)
        GL30.glShaderSource(vertexShader, vertexSource)
        GL30.glCompileShader(vertexShader)

        val fragmentShader = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER)
        GL30.glShaderSource(fragmentShader, fragmentSource)
        GL30.glCompileShader(fragmentShader)

        program = GL30.glCreateProgram()
        GL30.glAttachShader(program, vertexShader)
        GL30.glAttachShader(program, fragmentShader)
        GL30.glLinkProgram(program)
        GL30.glDeleteShader(vertexShader)
        GL30.glDeleteShader(fragmentShader)
    }
}