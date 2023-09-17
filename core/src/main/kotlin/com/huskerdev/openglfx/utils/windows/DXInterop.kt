package com.huskerdev.openglfx.utils.windows

import com.huskerdev.ojgl.GLContext


const val WGL_ACCESS_WRITE_DISCARD_NV = 0x2

class DXInterop {
    companion object {
        var interopHandle = 0L

        @JvmStatic private external fun nHasDXInterop(): Boolean
        @JvmStatic private external fun nHasRenderDocLib(): Boolean

        @JvmStatic external fun wglDXOpenDeviceNV(dxDevice: Long): Long
        @JvmStatic external fun wglDXCloseDeviceNV(hDevice: Long): Boolean
        @JvmStatic external fun wglDXRegisterObjectNV(device: Long, dxResource: Long, name: Int, type: Int, access: Int): Long
        @JvmStatic external fun wglDXSetResourceShareHandleNV(dxResource: Long, shareHandle: Long): Boolean
        @JvmStatic external fun wglDXUnregisterObjectNV(device: Long, obj: Long): Boolean
        @JvmStatic external fun wglDXLockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean
        @JvmStatic external fun wglDXUnlockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean

        @JvmStatic external fun createD3DTexture(device: Long, width: Int, height: Int): LongArray
        @JvmStatic external fun replaceD3DTextureInResource(resource: Long, newTexture: Long)

        val isSupported by lazy {
            if(nHasRenderDocLib()) {
                println("[OpenGLFX] WGL_NV_DX_interop is disabled due to RenderDoc library.")
                return@lazy false
            }

            val context = GLContext.create()
            context.makeCurrent()
            val result = nHasDXInterop()
            GLContext.delete(context)
            return@lazy result
        }
    }
}