package com.huskerdev.openglfx.lwjgl

import com.huskerdev.openglfx.OpenGLCanvas

@JvmField
val LWJGL_MODULE = LWJGLInitializer()

abstract class LWJGLCanvas: OpenGLCanvas() {
}