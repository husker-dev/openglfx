package scene.math

import org.lwjgl.BufferUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Matrix4(vararg val values: Float) {
    companion object {
        val identity = Matrix4(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )

        fun perspective(fov: Float, aspect: Float, near: Float, far: Float) =
            Matrix4(
                1/(aspect* tan(fov/2)), 0f, 0f, 0f,
                0f, 1/ tan(fov/2), 0f, 0f,
                0f, 0f, -(far+near)/(far-near), -(2*far*near)/(far-near),
                0f, 0f, -1f, 0f
            )

        fun lookAt(eye: Vec3, at: Vec3, up: Vec3 = Vec3(0f, 1f, 0f)): Matrix4 {
            var zAxis = (at - eye).normalized()
            val xAxis = (zAxis * up).normalized()
            val yAxis = xAxis * zAxis
            zAxis = -zAxis

            return Matrix4(
                xAxis.x, xAxis.y, xAxis.z, -xAxis.dot(eye),
                yAxis.x, yAxis.y, yAxis.z, -yAxis.dot(eye),
                zAxis.x, zAxis.y, zAxis.z, -zAxis.dot(eye),
                0f, 0f, 0f, 1f
            )
        }

        fun rotationX(angle: Float) =
            Matrix4(
                1f, 0f, 0f, 0f,
                0f, cos(angle), -sin(angle), 0f,
                0f, sin(angle), cos(angle), 0f,
                0f, 0f, 0f, 1f
            )

        fun rotationY(angle: Float) =
            Matrix4(
                cos(angle), 0f, sin(angle), 0f,
                0f, 1f, 0f, 0f,
                -sin(angle), 0f, cos(angle), 0f,
                0f, 0f, 0f, 1f
            )

        fun translate(x: Float, y: Float, z: Float) =
            Matrix4(
                1f, 0f, 0f, x,
                0f, 1f, 0f, y,
                0f, 0f, 1f, z,
                0f, 0f, 0f, 1f
            )
    }

    fun toByteBuffer() = BufferUtils.createFloatBuffer(values.size).apply { put(values); flip() }

    operator fun get(i: Int, r: Int) = values[i * 4 + r]

    operator fun times(matrix: Matrix4): Matrix4 {
        val result = FloatArray(16)
        for(i in 0..15){
            val line = i / 4
            val column = i % 4
            for(r in 0..3)
                result[i] += matrix[line, r] * this[r, column]
        }
        return Matrix4(*result)
    }

    override fun toString() = values.toList().chunked(4).joinToString("\n") { it.joinToString(",") }
}