package com.husker.openglfx

abstract class FXGLInitializer {

    abstract val name: String
    abstract val supportsDirect: Boolean
    abstract val supportsUniversal: Boolean

    abstract fun createDirect(): OpenGLCanvas
    abstract fun createUniversal(): OpenGLCanvas
}