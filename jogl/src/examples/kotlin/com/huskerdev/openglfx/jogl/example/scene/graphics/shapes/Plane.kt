package com.huskerdev.openglfx.jogl.example.scene.graphics.shapes

import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL3
import javafx.scene.paint.Color
import com.huskerdev.openglfx.jogl.example.scene.graphics.Mesh

class Plane(
    gl: GL3,
    width: Float,
    height: Float,
    y: Float = 0f,
    color: Color = Color.WHITE
): Mesh(
    gl,
    floatArrayOf(
        -width, y, -height,     color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat(),
         width, y, -height,     color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat(),
         width, y,  height,     color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat(),
        -width, y,  height,     color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat(),
    ),
    intArrayOf(0, 1, 2, 3), GL_TRIANGLE_FAN
)