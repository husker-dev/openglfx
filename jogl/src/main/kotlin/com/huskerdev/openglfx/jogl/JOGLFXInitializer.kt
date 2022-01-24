package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.FXGLInitializer
import com.huskerdev.openglfx.jogl.direct.JOGLDirect
import com.huskerdev.openglfx.jogl.universal.JOGLUniversal
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile

class JOGLFXInitializer: FXGLInitializer() {
    override val name = "JOGL"
    override val supportsDirect = true
    override val supportsUniversal = true

    override fun createDirect() = JOGLDirect()
    override fun createUniversal() = JOGLUniversal(GLCapabilities(GLProfile.getDefault()))
}