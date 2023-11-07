package com.huskerdev.openglfx.utils

import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.floatBuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glAttachShader
import com.huskerdev.openglfx.GLExecutor.Companion.glBindBuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glBindVertexArray
import com.huskerdev.openglfx.GLExecutor.Companion.glBufferData
import com.huskerdev.openglfx.GLExecutor.Companion.glCompileShader
import com.huskerdev.openglfx.GLExecutor.Companion.glCreateProgram
import com.huskerdev.openglfx.GLExecutor.Companion.glCreateShader
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteBuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteShader
import com.huskerdev.openglfx.GLExecutor.Companion.glDrawArrays
import com.huskerdev.openglfx.GLExecutor.Companion.glGenBuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glGenVertexArrays
import com.huskerdev.openglfx.GLExecutor.Companion.glLinkProgram
import com.huskerdev.openglfx.GLExecutor.Companion.glShaderSource
import com.huskerdev.openglfx.GLExecutor.Companion.glVertexAttribPointer
import com.huskerdev.openglfx.GLExecutor.Companion.glEnableVertexAttribArray
import com.huskerdev.openglfx.GLExecutor.Companion.glGetUniformLocation
import com.huskerdev.openglfx.GLExecutor.Companion.glUniform2f
import com.huskerdev.openglfx.GLExecutor.Companion.glUseProgram
import com.huskerdev.openglfx.utils.fbo.Framebuffer

class PassthroughShader {

    private val vertexSource = """
        #version 330 core
        layout (location = 0) in vec4 aPos;

        void main() {
            gl_Position = aPos;
        }
    """.trimIndent()

    private val fragmentSource = """
        #version 330 core
        
        uniform sampler2D tex;
        uniform vec2 tex_size;
        
        layout(location = 0) out vec4 out_color;
        
        void main() {
            out_color = texture(tex, gl_FragCoord.xy / tex_size);
        }  
    """.trimIndent()

    private val program: Int
    private val vao: Int
    private val texSizeLoc: Int

    init {
        val vertex = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertex, vertexSource)
        glCompileShader(vertex)

        val fragment = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragment, fragmentSource)
        glCompileShader(fragment)

        program = glCreateProgram()
        glAttachShader(program, vertex)
        glAttachShader(program, fragment)
        glLinkProgram(program)
        glDeleteShader(vertex)
        glDeleteShader(fragment)

        texSizeLoc = glGetUniformLocation(program, "tex_size")

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, floatBuffer(floatArrayOf(
            -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f
        )), GL_STATIC_DRAW)

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)

        glBindVertexArray(0)
        glDeleteBuffers(vbo)
    }

    fun copy(source: Framebuffer, target: Framebuffer){
        glUseProgram(program)
        glUniform2f(texSizeLoc, target.width.toFloat(), target.height.toFloat())

        glBindTexture(GL_TEXTURE_2D, source.texture)
        glBindFramebuffer(GL_FRAMEBUFFER, target.id)

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
    }
}