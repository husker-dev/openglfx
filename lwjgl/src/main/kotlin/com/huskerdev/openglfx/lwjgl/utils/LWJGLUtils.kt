package com.huskerdev.openglfx.lwjgl.utils


import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack.stackPush

class LWJGLUtils {

    companion object {
        fun rawGL(runnable: Runnable) {
            val stack = stackPush()

            val program = stack.mallocInt(1)
            glGetIntegerv(GL_CURRENT_PROGRAM, program)

            glUseProgram(0)
            glPushAttrib(GL_ALL_ATTRIB_BITS)
            glPushMatrix()

            runnable.run()

            glPopMatrix()
            glPopAttrib()
            glUseProgram(program[0])

            stack.close()
        }
    }
}