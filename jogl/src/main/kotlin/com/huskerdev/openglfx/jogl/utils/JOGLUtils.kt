package com.huskerdev.openglfx.jogl.utils

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2.*
import java.nio.IntBuffer

class JOGLUtils {

    companion object {
        fun rawGL(gl: GL2, runnable: Runnable) {
            val program = IntBuffer.allocate(1)
            gl.glGetIntegerv(GL_CURRENT_PROGRAM, program)

            val framebuffer = IntBuffer.allocate(1)
            gl.glGetIntegerv(GL_FRAMEBUFFER_BINDING, framebuffer)

            gl.glUseProgram(0)
            gl.glPushAttrib(GL_ALL_ATTRIB_BITS)
            gl.glPushMatrix()

            runnable.run()

            gl.glPopMatrix()
            gl.glPopAttrib()
            gl.glUseProgram(program[0])
            gl.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer[0])
        }
    }
}