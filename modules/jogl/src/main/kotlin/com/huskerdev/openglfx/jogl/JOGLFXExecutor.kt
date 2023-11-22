package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.GLProfile

@JvmField
val JOGL_MODULE = JOGLExecutor()

class JOGLExecutor: GLExecutor() {

    override fun blitCanvas(profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        if(async) JOGLAsyncBlitCanvas(this, profile, flipY, msaa)
        else JOGLBlitCanvas(this, profile, flipY, msaa)

    override fun sharedCanvas(profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        if(async) JOGLAsyncSharedCanvas(this, profile, flipY, msaa)
        else JOGLSharedCanvas(this, profile, flipY, msaa)

    override fun interopCanvas(profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        if(async) JOGLAsyncNVDXInteropCanvas(this, profile, flipY, msaa)
        else JOGLNVDXInteropCanvas(this, profile, flipY, msaa)
}