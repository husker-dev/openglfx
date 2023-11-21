package com.huskerdev.openglfx.internal.d3d9

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.internal.GLFXUtils

internal const val WGL_ACCESS_WRITE_DISCARD_NV = 0x2

class NVDXInterop {
    companion object {
        @JvmStatic private external fun hasNVDXInteropFunctions(): Boolean

        @JvmStatic private external fun wglDXOpenDeviceNV(dxDevice: Long): Long
        @JvmStatic private external fun wglDXCloseDeviceNV(hDevice: Long): Boolean
        @JvmStatic private external fun wglDXRegisterObjectNV(device: Long, dxResource: Long, name: Int, type: Int, access: Int): Long
        @JvmStatic private external fun wglDXSetResourceShareHandleNV(dxResource: Long, shareHandle: Long): Boolean
        @JvmStatic private external fun wglDXUnregisterObjectNV(device: Long, obj: Long): Boolean
        @JvmStatic private external fun wglDXLockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean
        @JvmStatic private external fun wglDXUnlockObjectsNV(interopHandle: Long, sharedTextureHandle: Long): Boolean

        init {
            GLFXUtils.loadLibrary()
            GLExecutor.loadBasicFunctionPointers()
        }

        internal val interopDevice by lazy {
            NVDXDevice(wglDXOpenDeviceNV(D3D9Device.fxInstance.handle))
        }

        internal fun linkShareHandle(dxResource: Long, shareHandle: Long) =
            wglDXSetResourceShareHandleNV(dxResource, shareHandle)

        fun isSupported() = hasNVDXInteropFunctions()
    }

    internal data class NVDXDevice(val handle: Long) {
        fun registerObject(dxResource: Long, name: Int, type: Int, access: Int) =
            NVDXObject(this, wglDXRegisterObjectNV(handle, dxResource, name, type, access))

        fun unregisterObject(obj: NVDXObject) =
            wglDXUnregisterObjectNV(handle, obj.handle)

        fun dispose() =
            wglDXCloseDeviceNV(handle)
    }

    internal data class NVDXObject(val device: NVDXDevice, val handle: Long) {
        fun lock() = wglDXLockObjectsNV(device.handle, handle)
        fun unlock() = wglDXUnlockObjectsNV(device.handle, handle)
        fun dispose() = device.unregisterObject(this)
    }
}