package com.husker.openglfx.lwjgl.utils


import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack

class LWJGLUtils {

    companion object {
        fun rawGL(runnable: Runnable) {
            MemoryStack.stackPush().use {
                val program = it.mallocInt(1)
                glGetIntegerv(GL_CURRENT_PROGRAM, program)

                glUseProgram(0)
                glPushAttrib(GL_ALL_ATTRIB_BITS)
                glPushMatrix()

                runnable.run()

                glPopMatrix()
                glPopAttrib()
                glUseProgram(program[0])
            }

        }
    }
}