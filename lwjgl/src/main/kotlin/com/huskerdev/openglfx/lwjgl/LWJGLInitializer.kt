package com.huskerdev.openglfx.lwjgl

import com.huskerdev.openglfx.FXGLInitializer
import com.huskerdev.openglfx.lwjgl.direct.LWJGLDirect
import com.huskerdev.openglfx.lwjgl.universal.LWJGLUniversal

class LWJGLInitializer: FXGLInitializer() {
    override val name = "LWJGL"
    override val supportsDirect = true
    override val supportsUniversal = true

    override fun createDirect() = LWJGLDirect()
    override fun createUniversal() = LWJGLUniversal()
}