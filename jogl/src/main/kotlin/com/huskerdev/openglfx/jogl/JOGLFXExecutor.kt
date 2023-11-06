package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.GLExecutor

@JvmField
val JOGL_MODULE = JOGLExecutor()

class JOGLExecutor: GLExecutor() {

    override fun universalCanvas(profile: com.huskerdev.openglfx.GLProfile, flipY: Boolean, msaa: Int, multiThread: Boolean) =
        JOGLUniversalCanvas(this, profile, flipY, msaa)
    override fun sharedCanvas(profile: com.huskerdev.openglfx.GLProfile, flipY: Boolean, msaa: Int, multiThread: Boolean) =
        JOGLSharedCanvas(this, profile, flipY, msaa)
    override fun interopCanvas(profile: com.huskerdev.openglfx.GLProfile, flipY: Boolean, msaa: Int, multiThread: Boolean) =
        JOGLInteropCanvas(this, profile, flipY, msaa)

    override fun initGLFunctionsImpl() {}
}