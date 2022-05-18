package com.huskerdev.openglfx.utils

import java.io.FileOutputStream

class WinUtils {
    companion object {

        private var initialized = false
        init {
             loadLibrary()
        }

        @JvmStatic external fun wglDXLockObjectsNV(funP: Long, interopHandle: Long, sharedTextureHandle: Long): Boolean
        @JvmStatic external fun wglDXUnlockObjectsNV(funP: Long, interopHandle: Long, sharedTextureHandle: Long): Boolean
        @JvmStatic external fun createD3DTexture(device: Long, width: Int, height: Int): LongArray
        @JvmStatic external fun replaceD3DTextureInResource(resource: Long, newTexture: Long)
        @JvmStatic external fun createGLContext(isCore: Boolean, shareRc: Long, choosePixelPtr: Long, createContextPtr: Long): LongArray

        fun loadLibrary(){
            if(initialized) return
            initialized = true

            val fileName = "win_utils_${OpenGLFXUtils.archName}.dll"
            val tmpFileName = "${System.getProperty("java.io.tmpdir")}/$fileName"

            try {
                val inputStream = this::class.java.getResourceAsStream("/com/huskerdev/openglfx/natives/$fileName")!!
                FileOutputStream(tmpFileName).use {
                    inputStream.transferTo(it)
                }
            }catch (_: Exception){}
            System.load(tmpFileName)
        }
    }
}