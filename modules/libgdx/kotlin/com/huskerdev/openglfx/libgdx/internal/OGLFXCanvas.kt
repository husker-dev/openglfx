package com.huskerdev.openglfx.libgdx.internal

import com.huskerdev.openglfx.canvas.GLCanvas

class OGLFXCanvas(val canvas: GLCanvas) {

    val graphics = OGLFXGraphics(canvas)
}