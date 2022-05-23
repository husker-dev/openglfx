package com.huskerdev.openglfx.utils

class LinuxUtils {

    companion object {
        init {
            OpenGLFXUtils.loadLibrary()
        }

        @JvmStatic external fun createContext(isCore: Boolean, shareWith: Long): LongArray
        @JvmStatic external fun getCurrentContext(): LongArray
        @JvmStatic external fun setCurrentContext(display: Long, window: Long, context: Long): Boolean
    }
}