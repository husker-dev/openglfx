package com.husker.openglfx

import com.jogamp.opengl.GL

interface FXGLEventListener {

    fun display(gl: GL)
    fun reshape(gl: GL, width: Float, height: Float)
    fun init(gl: GL)
    fun dispose(gl: GL)
}