package com.huskerdev.openglfx.lwjgl

import com.huskerdev.openglfx.GLExecutor
import org.lwjgl.opengl.GL


class LWJGLExecutor: GLExecutor() {

    companion object {
        @JvmField val LWJGL_MODULE = LWJGLExecutor()
    }

    override fun initGLFunctionsImpl() {
        GL.createCapabilities()
    }
}