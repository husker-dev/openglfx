package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.GLProfile

@JvmField
val JOGL_MODULE = JOGLExecutor()

class JOGLExecutor: GLExecutor() {

    override fun blitCanvas(profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        JOGLUniversalCanvas(this, profile, flipY, msaa)
    override fun sharedCanvas(profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        JOGLSharedCanvas(this, profile, flipY, msaa)
    override fun interopCanvas(profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        JOGLInteropCanvas(this, profile, flipY, msaa)

    override fun initGLFunctions() {}
}