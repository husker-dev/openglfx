package com.husker.openglfx.utils

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2
import com.sun.prism.GraphicsPipeline
import javafx.scene.layout.Region
import java.nio.IntBuffer

class FXUtils {
    companion object {

        val pipelineName: String
            get() = GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]

        fun rawGL(gl: GL2, region: Region, runnable: Runnable) {
            val program = IntBuffer.allocate(1)
            gl.glGetIntegerv(GL2ES2.GL_CURRENT_PROGRAM, program)

            gl.glUseProgram(0)
            gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS)
            gl.glPushMatrix()

            gl.glTranslated(-region.width / 2, -region.height / 2, 0.0)
            runnable.run()

            gl.glPopMatrix()
            gl.glPopAttrib()
            gl.glUseProgram(program[0])
        }
    }
}