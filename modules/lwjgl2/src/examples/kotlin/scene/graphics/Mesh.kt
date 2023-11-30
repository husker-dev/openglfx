package scene.graphics

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import scene.math.Matrix4
import scene.toDirectBuffer

open class Mesh(
    vertices: FloatArray,
    private val indices: IntArray,
    private val renderType: Int,
    transform: Matrix4 = Matrix4.identity
){
    private val vao: Int = glGenVertexArrays()
    private val transform = transform.toByteBuffer()

    init {
        val vbo = glGenBuffers()
        val ebo = glGenBuffers()

        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices.toDirectBuffer(), GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.toDirectBuffer(), GL_STATIC_DRAW)

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.SIZE_BYTES, 0L)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 7 * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
        glEnableVertexAttribArray(1)

        glBindVertexArray(0)
    }

    fun render(transformLocation: Int){
        glBindVertexArray(vao)
        glUniformMatrix4(transformLocation, true, transform)
        glDrawElements(renderType, indices.size, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }
}