package com.huskerdev.openglfx.internal.shaders

import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glAttachShader
import com.huskerdev.openglfx.GLExecutor.Companion.glBindBuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glBindVertexArray
import com.huskerdev.openglfx.GLExecutor.Companion.glBufferData
import com.huskerdev.openglfx.GLExecutor.Companion.glCompileShader
import com.huskerdev.openglfx.GLExecutor.Companion.glCreateProgram
import com.huskerdev.openglfx.GLExecutor.Companion.glCreateShader
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteShader
import com.huskerdev.openglfx.GLExecutor.Companion.glDrawArrays
import com.huskerdev.openglfx.GLExecutor.Companion.glGenBuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glGenVertexArrays
import com.huskerdev.openglfx.GLExecutor.Companion.glLinkProgram
import com.huskerdev.openglfx.GLExecutor.Companion.glShaderSource
import com.huskerdev.openglfx.GLExecutor.Companion.glVertexAttribPointer
import com.huskerdev.openglfx.GLExecutor.Companion.glEnableVertexAttribArray
import com.huskerdev.openglfx.GLExecutor.Companion.glGetAttribLocation
import com.huskerdev.openglfx.GLExecutor.Companion.glGetShaderInfoLog
import com.huskerdev.openglfx.GLExecutor.Companion.glGetShaderi
import com.huskerdev.openglfx.GLExecutor.Companion.glGetUniformLocation
import com.huskerdev.openglfx.GLExecutor.Companion.glUniform2f
import com.huskerdev.openglfx.GLExecutor.Companion.glUseProgram
import com.huskerdev.openglfx.internal.Framebuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal open class PassthroughShader(
    vertexSource: String = """
        attribute vec4 position;

        void main() {
            gl_Position = position;
        }
    """.trimIndent(),
    fragmentSource: String = """
        uniform sampler2D texture;
        uniform vec2 size;
        
        void main() {
            gl_FragColor = texture2D(texture, gl_FragCoord.xy / size);
        }
    """.trimIndent()
) {
    private val program: Int
    private val vao: Int
    private val positionLoc: Int
    private val sizeLoc: Int

    init {
        val vertex = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertex, vertexSource)
        glCompileShader(vertex)
        if(glGetShaderi(vertex, GL_COMPILE_STATUS) == 0)
            throw Exception("Compilation error in vertex shader: \n ${glGetShaderInfoLog(vertex)}")

        val fragment = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragment, fragmentSource)
        glCompileShader(fragment)
        if(glGetShaderi(fragment, GL_COMPILE_STATUS) == 0)
            throw Exception("Compilation error in fragment shader: \n ${glGetShaderInfoLog(fragment)}")

        program = glCreateProgram()
        glAttachShader(program, vertex)
        glAttachShader(program, fragment)
        glLinkProgram(program)
        glDeleteShader(vertex)
        glDeleteShader(fragment)

        positionLoc = glGetAttribLocation(program, "position")
        sizeLoc = glGetUniformLocation(program, "size")

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(
            GL_ARRAY_BUFFER,
            floatBuffer(floatArrayOf(-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f)),
            GL_STATIC_DRAW
        )

        glVertexAttribPointer(positionLoc, 2, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(positionLoc)

        glBindVertexArray(0)
    }

    private fun floatBuffer(array: FloatArray) =
        ByteBuffer.allocateDirect(array.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(array)

    fun apply(source: Framebuffer.Default, target: Framebuffer) =
        apply(source.texture, target)

    fun apply(sourceTexture: Int, target: Framebuffer){
        glUseProgram(program)
        glUniform2f(sizeLoc, target.width.toFloat(), target.height.toFloat())

        glBindTexture(GL_TEXTURE_2D, sourceTexture)
        glBindFramebuffer(GL_FRAMEBUFFER, target.id)

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
    }
}