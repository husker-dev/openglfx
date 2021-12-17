package com.husker.openglfx.lwjgl

import com.husker.openglfx.FXGLInitializer
import com.husker.openglfx.lwjgl.direct.LWJGLDirect
import com.husker.openglfx.lwjgl.universal.LWJGLUniversal

class LWJGLInitializer: FXGLInitializer() {
    override val name = "LWJGL"
    override val supportsDirect = true
    override val supportsUniversal = true

    override fun createDirect() = LWJGLDirect()
    override fun createUniversal() = LWJGLUniversal()
}