package com.huskerdev.openglfx.utils.d3d9

import com.huskerdev.openglfx.utils.OpenGLFXUtils
import com.sun.prism.Texture
import java.io.FileOutputStream

class D3D9Utils {
    companion object {
        @JvmStatic external fun wglDXLockObjectsNV(funP: Long, interopHandle: Long, sharedTextureHandle: Long)
        @JvmStatic external fun wglDXUnlockObjectsNV(funP: Long, interopHandle: Long, sharedTextureHandle: Long)
        @JvmStatic external fun replaceTextureInResource(resource: Long, newTexture: Long)

        val Texture.textureResource: Long
            get() = Class.forName("com.sun.prism.d3d.D3DTexture")
                .getMethod("getNativeSourceHandle")
                .apply { isAccessible = true }
                .invoke(this) as Long

        fun loadLibrary(){
            val fileName = "openglfx_${OpenGLFXUtils.archName}.dll"
            val tmpFileName = "${System.getProperty("java.io.tmpdir")}/$fileName"

            val inputStream = this::class.java.getResourceAsStream("/com/huskerdev/openglfx/natives/$fileName")!!
            FileOutputStream(tmpFileName).use {
                inputStream.transferTo(it)
            }
            System.load(tmpFileName)
        }
    }
}