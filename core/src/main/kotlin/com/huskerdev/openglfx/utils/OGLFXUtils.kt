package com.huskerdev.openglfx.utils

import com.sun.javafx.tk.PlatformImage
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Texture
import javafx.scene.image.Image


class OGLFXUtils {

    companion object {
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