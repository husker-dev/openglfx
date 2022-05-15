package com.huskerdev.openglfx.utils.d3d9

class D3D9Texture(
    val handle: Long,
    val sharedHandle: Long
){

    companion object {
        @JvmStatic private external fun releaseTexture(handle: Long): LongArray
    }

    fun release() = releaseTexture(handle)
}