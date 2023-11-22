package com.huskerdev.openglfx.jogl.example.scene.graphics

import com.huskerdev.openglfx.jogl.example.scene.*
import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL3
import com.huskerdev.openglfx.jogl.example.scene.math.Matrix4
import com.huskerdev.openglfx.jogl.example.scene.toDirectBuffer

open class Mesh(
    gl: GL3,
    vertices: FloatArray,
    private val indices: IntArray,
    private val renderType: Int,
    transform: Matrix4 = Matrix4.identity
){
    private var vao = 0
    private val transform = transform.toByteBuffer()

    init {
        vao = useDirectIntBuffer(1, 0) { gl.glGenVertexArrays(1, this) }
        val vbo = useDirectIntBuffer(1, 0) { gl.glGenBuffers(1, this) }
        val ebo = useDirectIntBuffer(1, 0) { gl.glGenBuffers(1, this) }

        gl.glBindVertexArray(vao)
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo)
        gl.glBufferData(GL_ARRAY_BUFFER, vertices.size.toLong() * Float.SIZE_BYTES, vertices.toDirectBuffer(), GL_STATIC_DRAW)
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size.toLong() * Int.SIZE_BYTES, indices.toDirectBuffer(), GL_STATIC_DRAW)

        gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.SIZE_BYTES, 0L)
        gl.glEnableVertexAttribArray(0)
        gl.glVertexAttribPointer(1, 4, GL_FLOAT, false, 7 * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
        gl.glEnableVertexAttribArray(1)

        gl.glBindVertexArray(0)
    }

    fun render(gl: GL3, transformLocation: Int){
        gl.glBindVertexArray(vao)
        gl.glUniformMatrix4fv(transformLocation, 1, true, transform)
        gl.glDrawElements(renderType, indices.size, GL_UNSIGNED_INT, 0)
        gl.glBindVertexArray(0)
    }
}