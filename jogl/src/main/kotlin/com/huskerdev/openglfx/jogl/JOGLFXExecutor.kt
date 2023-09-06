package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.GLExecutor

@JvmField
val JOGL_MODULE = JOGLExecutor()

class JOGLExecutor: GLExecutor() {

    override fun universalCanvas(profile: com.huskerdev.openglfx.GLProfile) = JOGLUniversalCanvas(this, profile)
    override fun sharedCanvas(profile: com.huskerdev.openglfx.GLProfile) = JOGLSharedCanvas(this, profile)
    override fun interopCanvas(profile: com.huskerdev.openglfx.GLProfile) = JOGLInteropCanvas(this, profile)

    override fun initGLFunctionsImpl() {}

}