package com.husker.openglfx


interface FXGLEventListener {

    fun display()
    fun reshape(width: Float, height: Float)
    fun init()
    fun dispose()
}