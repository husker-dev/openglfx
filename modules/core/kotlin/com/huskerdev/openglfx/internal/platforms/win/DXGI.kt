package com.huskerdev.openglfx.internal.platforms.win

import com.huskerdev.openglfx.internal.GLFXUtils


class DXGI {
    companion object {
        const val GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT = 0x958C

        init {
            GLFXUtils.loadLibrary()
        }

        @JvmStatic external fun glCreateMemoryObjectsEXT(): Int
        @JvmStatic external fun glDeleteMemoryObjectsEXT(memoryObject: Int)

        @JvmStatic external fun glImportMemoryWin32HandleEXT(memory: Int, size: Long, handleType: Int, handle: Long)
        @JvmStatic external fun glTextureStorageMem2DEXT(texture: Int, levels: Int, internalFormat: Int, width: Int, height: Int, memory: Int, offset: Long)
    }
}