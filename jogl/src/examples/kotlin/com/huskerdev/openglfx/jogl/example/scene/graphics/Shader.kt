package com.huskerdev.openglfx.jogl.example.scene.graphics

import com.jogamp.opengl.GL3
import com.jogamp.opengl.GL3.GL_FRAGMENT_SHADER
import com.jogamp.opengl.GL3.GL_VERTEX_SHADER


class Shader(gl: GL3, vertexSource: String, fragmentSource: String) {
    val program: Int

    init {
        val vertexShader = gl.glCreateShader(GL_VERTEX_SHADER)
        gl.glShaderSource(vertexShader, 1, arrayOf(vertexSource), arrayOf(vertexSource.length).toIntArray(), 0)
        gl.glCompileShader(vertexShader)

        val fragmentShader = gl.glCreateShader(GL_FRAGMENT_SHADER)
        gl.glShaderSource(fragmentShader, 1, arrayOf(fragmentSource), arrayOf(fragmentSource.length).toIntArray(), 0)
        gl.glCompileShader(fragmentShader)

        program = gl.glCreateProgram()
        gl.glAttachShader(program, vertexShader)
        gl.glAttachShader(program, fragmentShader)
        gl.glLinkProgram(program)
        gl.glDeleteShader(vertexShader)
        gl.glDeleteShader(fragmentShader)
    }
}