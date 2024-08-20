package com.huskerdev.openglfx.internal.platforms

import com.huskerdev.openglfx.internal.GLFXUtils

const val GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT = 0x958C
const val GL_HANDLE_TYPE_OPAQUE_FD_EXT = 0x9586

class MemoryObjects {
    companion object {
        @JvmStatic external fun nLoadFunctions()
        @JvmStatic external fun glCreateMemoryObjectsEXT(): Int
        @JvmStatic external fun glDeleteMemoryObjectsEXT(memoryObject: Int)
        @JvmStatic external fun glTextureStorageMem2DEXT(texture: Int, levels: Int, internalFormat: Int, width: Int, height: Int, memory: Int, offset: Long)

        @JvmStatic external fun glImportMemoryWin32HandleEXT(memory: Int, size: Long, handleType: Int, handle: Long)
        @JvmStatic external fun glImportMemoryFdEXT(memory: Int, size: Long, handleType: Int, fd: Int)

        init {
            GLFXUtils.loadLibrary()
            nLoadFunctions()
        }
    }
}