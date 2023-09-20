package com.huskerdev.openglfx.utils.windows

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor


const val WGL_ACCESS_WRITE_DISCARD_NV = 0x2

class DXInterop {
    companion object {

        @JvmStatic external fun wglDXOpenDeviceNV(dxDevice: Long): Long
        @JvmStatic external fun wglDXCloseDeviceNV(hDevice: Long): Boolean
        @JvmStatic external fun wglDXRegisterObjectNV(device: Long, dxResource: Long, name: Int, type: Int, access: Int): Long
        @JvmStatic external fun wglDXSetResourceShareHandleNV(dxResource: Long, shareHandle: Long): Boolean
        @JvmStatic external fun wglDXUnregisterObjectNV(device: Long, obj: Long): Boolean
        @JvmStatic external fun wglDXLockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean
        @JvmStatic external fun wglDXUnlockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean

        @JvmStatic external fun createD3DTexture(device: Long, width: Int, height: Int): LongArray
        @JvmStatic external fun replaceD3DTextureInResource(resource: Long, newTexture: Long)

        val interopHandle by lazy {
            var context = GLContext.current()
            return@lazy if(context.handle == 0L) {
                context = GLContext.create()
                context.makeCurrent()

                val result = createInteropHandle()
                GLContext.delete(context)
                result
            } else createInteropHandle()
        }

        private fun createInteropHandle(): Long {
            GLExecutor.initGLFunctions()
            return wglDXOpenDeviceNV(D3D9Device.fxInstance.handle)
        }

        fun isSupported() = interopHandle != 0L
    }
}