package com.huskerdev.openglfx.utils

const val WGL_ACCESS_WRITE_DISCARD_NV = 0x2
//const val WGL_ACCESS_READ_WRITE_NV = 0x1

class WinUtils {
    companion object {

        init {
            OpenGLFXUtils.loadLibrary()
        }

        @JvmStatic external fun getCurrentContext(): LongArray
        @JvmStatic external fun setCurrentContext(dc: Long, rc: Long): Boolean
        @JvmStatic external fun createContext(isCore: Boolean, shareWith: Long): LongArray

        @JvmStatic external fun hasDXInterop(): Boolean
        @JvmStatic external fun wglDXOpenDeviceNV(dxDevice: Long): Long
        @JvmStatic external fun wglDXRegisterObjectNV(device: Long, dxResource: Long, name: Int, type: Int, access: Int): Long
        @JvmStatic external fun wglDXSetResourceShareHandleNV(dxResource: Long, shareHandle: Long): Boolean
        @JvmStatic external fun wglDXUnregisterObjectNV(device: Long, obj: Long): Boolean
        @JvmStatic external fun wglDXLockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean
        @JvmStatic external fun wglDXUnlockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean

        @JvmStatic external fun createD3DTexture(device: Long, width: Int, height: Int): LongArray
        @JvmStatic external fun replaceD3DTextureInResource(resource: Long, newTexture: Long)
    }
}