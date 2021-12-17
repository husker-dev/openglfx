package com.husker.openglfx.jogl

import com.husker.openglfx.FXGLInitializer
import com.husker.openglfx.jogl.direct.JOGLDirect
import com.husker.openglfx.jogl.universal.JOGLUniversal
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile

class JOGLFXInitializer: FXGLInitializer() {
    override val name = "JOGL"
    override val supportsDirect = true
    override val supportsUniversal = true

    override fun createDirect() = JOGLDirect()
    override fun createUniversal() = JOGLUniversal(GLCapabilities(GLProfile.getDefault()))
}