package com.huskerdev.openglfx.lwjgl.utils


import org.lwjgl.opengl.GL30.*

class LWJGLUtils {

    companion object {
        fun rawGL(runnable: Runnable) {
            val program = glGetInteger(GL_CURRENT_PROGRAM)
            val buffer = glGetInteger(GL_FRAMEBUFFER_BINDING)

            // Reset configuration
            glUseProgram(0)
            glPushAttrib(GL_ALL_ATTRIB_BITS)

            // Render
            runnable.run()

            // Back to FX configuration
            glPopAttrib()
            glUseProgram(program)
            glBindFramebuffer(GL_FRAMEBUFFER, buffer)
        }
    }
}