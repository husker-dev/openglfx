package com.huskerdev.openglfx.internal.platforms.win

import com.huskerdev.openglfx.internal.GLFXUtils
import com.sun.glass.ui.Screen

class D3D9 {

    companion object {
        @JvmStatic private external fun nGetDeviceFromAdapter(screenOrdinal: Int): Long

        @JvmStatic private external fun nCreateDeviceEx(): Long
        @JvmStatic private external fun nCreateTexture(device: Long, width: Int, height: Int, shareHandle: Long): LongArray
        @JvmStatic private external fun nReleaseTexture(handle: Long)

        @JvmStatic external fun nStretchRect(device: Long, src: Long, dst: Long)
        @JvmStatic external fun nGetTexture(device: Long, stage: Int): Long

        init {
            GLFXUtils.loadLibrary()
        }
    }


    data class Device(
        val handle: Long = nCreateDeviceEx()
    ) {
        companion object {
            val jfx: Device by lazy {
                Device(nGetDeviceFromAdapter(Screen.getMainScreen().adapterOrdinal))
            }
        }

        fun createTexture(width: Int, height: Int, shareHandle: Long = 0) =
            nCreateTexture(handle, width, height, shareHandle).run {
                Texture(width, height, this[0], this[1])
            }

        fun stretchRect(src: Long, dst: Long) =
            nStretchRect(handle, src, dst)

        fun getTexture(stage: Int) =
            nGetTexture(handle, stage)
    }

    data class Texture(
        val width: Int,
        val height: Int,
        val handle: Long,
        val sharedHandle: Long
    ) {
        fun release() =
            nReleaseTexture(handle)
    }
}