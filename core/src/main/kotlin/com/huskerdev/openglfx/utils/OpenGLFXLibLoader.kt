package com.huskerdev.openglfx.utils

import com.huskerdev.ojgl.utils.OS
import com.huskerdev.ojgl.utils.PlatformUtils

class OpenGLFXLibLoader {

    companion object {
        private var isLoaded = false

        fun load(){
            if(isLoaded) return
            isLoaded = true

            val basename = "openglfx"
            val fileName = when(PlatformUtils.os) {
                OS.Windows, OS.Linux    -> "$basename-${PlatformUtils.arch}.${PlatformUtils.dynamicLibExt}"
                OS.MacOS                -> "$basename.dylib"
                else -> throw UnsupportedOperationException("Unsupported OS")
            }
            PlatformUtils.loadLibraryFromResources("/com/huskerdev/openglfx/natives/$fileName")
        }
    }
}