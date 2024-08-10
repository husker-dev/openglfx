package com.huskerdev.openglfx.internal.platforms.win

import com.huskerdev.openglfx.internal.GLFXUtils


class WGLDX {
    companion object {
        const val WGL_ACCESS_WRITE_DISCARD_NV = 0x2

        init {
            GLFXUtils.loadLibrary()
        }

        @JvmStatic private external fun wglDXOpenDeviceNV(dxDevice: Long): Long
        @JvmStatic private external fun wglDXCloseDeviceNV(hDevice: Long): Boolean
        @JvmStatic private external fun wglDXRegisterObjectNV(device: Long, dxResource: Long, name: Int, type: Int, access: Int): Long
        @JvmStatic private external fun wglDXSetResourceShareHandleNV(dxResource: Long, shareHandle: Long): Boolean
        @JvmStatic private external fun wglDXUnregisterObjectNV(device: Long, obj: Long): Boolean
        @JvmStatic private external fun wglDXLockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean
        @JvmStatic private external fun wglDXUnlockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean

        internal fun linkShareHandle(dxResource: Long, shareHandle: Long) =
            wglDXSetResourceShareHandleNV(dxResource, shareHandle)

        fun isSupported() = true
    }

    class Device(
        d3d9Device: D3D9.Device
    ){
        val handle = wglDXOpenDeviceNV(d3d9Device.handle)

        fun registerObject(dxResource: Long, name: Int, type: Int, access: Int) =
            Object(this, wglDXRegisterObjectNV(handle, dxResource, name, type, access))

        fun release() =
            wglDXCloseDeviceNV(handle)
    }


    class Object(
        val device: Device,
        val handle: Long
    ) {
        fun lock() =
            wglDXLockObjectsNV(device.handle, handle)

        fun unlock() =
            wglDXUnlockObjectsNV(device.handle, handle)

        fun release() =
            wglDXUnregisterObjectNV(device.handle, handle)
    }
}