package scene.graphics

import org.lwjgl.opengl.GL30
import scene.math.Matrix4

open class Mesh(
    vertices: FloatArray,
    private val indices: IntArray,
    private val renderType: Int,
    transform: Matrix4 = Matrix4.identity
){
    private val vao: Int = GL30.glGenVertexArrays()
    private val transform = transform.toByteBuffer()

    init {
        val vbo = GL30.glGenBuffers()
        val ebo = GL30.glGenBuffers()

        GL30.glBindVertexArray(vao)
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo)
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW)
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, ebo)
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW)

        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 7 * Float.SIZE_BYTES, 0L)
        GL30.glEnableVertexAttribArray(0)
        GL30.glVertexAttribPointer(1, 4, GL30.GL_FLOAT, false, 7 * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
        GL30.glEnableVertexAttribArray(1)

        GL30.glBindVertexArray(0)
    }

    fun render(transformLocation: Int){
        GL30.glBindVertexArray(vao)
        GL30.glUniformMatrix4fv(transformLocation, true, transform)
        GL30.glDrawElements(renderType, indices.size, GL30.GL_UNSIGNED_INT, 0)
        GL30.glBindVertexArray(0)
    }
}