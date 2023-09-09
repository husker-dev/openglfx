package scene.graphics.shapes

import javafx.scene.paint.Color
import org.lwjgl.opengl.GL30
import scene.graphics.Mesh
import scene.math.Matrix4

class Cube(
    color1: Color,
    color2: Color,
    color3: Color,
    color4: Color,
    color5: Color,
    color6: Color,
    color7: Color,
    color8: Color,
    transform: Matrix4 = Matrix4.identity
): Mesh(floatArrayOf(
    -1f, -1f,  1f,  color1.red.toFloat(), color1.green.toFloat(), color1.blue.toFloat(), color1.opacity.toFloat(),
     1f, -1f,  1f,  color2.red.toFloat(), color2.green.toFloat(), color2.blue.toFloat(), color2.opacity.toFloat(),
    -1f,  1f,  1f,  color3.red.toFloat(), color3.green.toFloat(), color3.blue.toFloat(), color3.opacity.toFloat(),
     1f,  1f,  1f,  color4.red.toFloat(), color4.green.toFloat(), color4.blue.toFloat(), color4.opacity.toFloat(),
    -1f, -1f, -1f,  color5.red.toFloat(), color5.green.toFloat(), color5.blue.toFloat(), color5.opacity.toFloat(),
     1f, -1f, -1f,  color6.red.toFloat(), color6.green.toFloat(), color6.blue.toFloat(), color6.opacity.toFloat(),
    -1f,  1f, -1f,  color7.red.toFloat(), color7.green.toFloat(), color7.blue.toFloat(), color7.opacity.toFloat(),
     1f,  1f, -1f,  color8.red.toFloat(), color8.green.toFloat(), color8.blue.toFloat(), color8.opacity.toFloat(),
), intArrayOf(0, 1, 2, 3, 7, 1, 5, 4, 7, 6, 2, 4, 0, 1), GL30.GL_TRIANGLE_STRIP, transform) {
    constructor(color: Color, transform: Matrix4): this(color, color, color, color, color, color, color, color, transform)
}
