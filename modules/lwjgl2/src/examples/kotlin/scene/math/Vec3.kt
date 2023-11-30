package scene.math

import kotlin.math.sqrt

class Vec3(val x: Float, val y: Float, val z: Float) {

    operator fun unaryMinus() =
        Vec3(-x, -y, -z)

    operator fun minus(vec: Vec3) =
        Vec3(x - vec.x, y - vec.y, z - vec.z)

    operator fun times(vec: Vec3) =
        Vec3(y*vec.z - z*vec.y, z*vec.x - x*vec.z, x*vec.y - y*vec.x)

    fun normalized(): Vec3 {
        val length = sqrt(x*x + y*y + z*z)
        return Vec3(x / length, y / length, z / length)
    }

    fun dot(vec: Vec3) = x*vec.x + y*vec.y + z*vec.z
}