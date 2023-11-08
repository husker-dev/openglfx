package com.huskerdev.openglfx.internal

import com.huskerdev.ojgl.utils.OS
import com.huskerdev.ojgl.utils.PlatformUtils
import com.sun.javafx.tk.PlatformImage
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Texture
import javafx.scene.image.Image


class OGLFXUtils {

    companion object {
        private var isLibLoaded = false

        fun loadLibrary(){
            if(isLibLoaded) return
            isLibLoaded = true

            val basename = "openglfx"
            val fileName = when(PlatformUtils.os) {
                OS.Windows, OS.Linux    -> "$basename-${PlatformUtils.arch}.${PlatformUtils.dynamicLibExt}"
                OS.MacOS                -> "$basename.dylib"
                else -> throw UnsupportedOperationException("Unsupported OS")
            }
            PlatformUtils.loadLibraryFromResources("/com/huskerdev/openglfx/natives/$fileName")
        }

        val Texture.DX9TextureResource: Long
            get() = Class.forName("com.sun.prism.d3d.D3DTexture")
                .getMethod("getNativeSourceHandle")
                .apply { isAccessible = true }
                .invoke(this) as Long

        val Texture.GLTextureId: Int
            get() = Class.forName("com.sun.prism.es2.ES2RTTexture")
                .getMethod("getNativeSourceHandle")
                .apply { isAccessible = true }
                .invoke(this) as Int

        fun Image.getPlatformImage() = Toolkit.getImageAccessor().getPlatformImage(this) as PlatformImage
    }
}