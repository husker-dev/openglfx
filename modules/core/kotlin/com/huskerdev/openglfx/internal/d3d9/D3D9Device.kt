package com.huskerdev.openglfx.internal.d3d9

import com.sun.glass.ui.Screen

internal class D3D9Device(val handle: Long) {

    companion object {
        @JvmStatic private external fun nGetDeviceFromAdapter(screenOrdinal: Int): Long

        @JvmStatic private external fun createD3DTexture(device: Long, width: Int, height: Int): LongArray
        @JvmStatic external fun replaceD3DTextureInResource(resource: Long, newTexture: Long)

        val fxInstance: D3D9Device by lazy {
            D3D9Device(nGetDeviceFromAdapter(Screen.getMainScreen().adapterOrdinal))
        }
    }

    fun createTexture(width: Int, height: Int) =
        createD3DTexture(handle, width, height).run { D3D9Texture(this[0], this[1]) }

}