package com.huskerdev.openglfx.lwjgl2
import com.huskerdev.grapl.gl.GLContext
import com.huskerdev.openglfx.GLExecutor

class LWJGL2Executor: GLExecutor() {

    companion object {
        @JvmField val LWJGL2_MODULE = LWJGL2Executor()
    }

    override fun initGLFunctions() {
        super.initGLFunctions()
        org.lwjgl.opengl.GLContext.useContext(GLContext.current().handle)
    }
}