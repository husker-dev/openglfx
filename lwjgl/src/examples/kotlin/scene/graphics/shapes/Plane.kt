package scene.graphics.shapes

import javafx.scene.paint.Color
import org.lwjgl.opengl.GL30
import scene.graphics.Mesh

class Plane(
    width: Float,
    height: Float,
    y: Float = 0f,
    color: Color = Color.WHITE
): Mesh(
    floatArrayOf(
        -width, y, -height,     color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat(),
         width, y, -height,     color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat(),
         width, y,  height,     color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat(),
        -width, y,  height,     color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat(),
    ),
    intArrayOf(0, 1, 2, 3), GL30.GL_TRIANGLE_FAN
)