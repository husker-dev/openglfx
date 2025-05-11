package com.huskerdev.openglfx.internal

import com.huskerdev.grapl.core.platform.OS
import com.huskerdev.grapl.core.platform.Platform
import com.huskerdev.grapl.gl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glGetError
import com.huskerdev.openglfx.GL_BGRA
import com.huskerdev.openglfx.GL_TEXTURE_2D
import com.huskerdev.openglfx.internal.platforms.GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glCreateMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glImportMemoryWin32HandleEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glTextureStorageMem2DEXT
import com.huskerdev.openglfx.internal.platforms.win.D3D9

enum class GLInteropType {
    /**
     *  Reads pixels by 'glBlitFramebuffer' and then creates JavaFX image.
     *
     *  - Supported platforms: **All**
     */
    Blit,

    /**
     *  Uses WGL_NV_DX_interop extension to convert GL framebuffer to DirectX 9 textures.
     *
     *  - Supported platforms: **Windows**
     */
    WGLDXInterop,

    /**
     *  Creates shared object between GL and DirectX
     *
     *  - Supported platforms: **Windows**
     */
    ExternalObjectsD3D,

    /**
     *  Creates shared object between two GL context using Vulkan proxy
     *
     *  - Supported platforms: **Windows**
     */
    ExternalObjectsESWin,

    /**
     *  Creates shared object between two GL context using Vulkan proxy
     *
     *  - Supported platforms: **Linux**
     */
    ExternalObjectsESLinux,

    /**
    *  Creates memory block in VRAM that can be used in different OpenGL contexts.
    *
    *  - Supported platforms: **macOS**
    */
    IOSurface;

    companion object {
        val auto: GLInteropType by lazy {

            val pipeline = GLFXUtils.pipeline

            var hasWGLNVInteropExt = false
            var hasMemoryObjectExt = false

            // Create temporary context to fetch extension information (not on macOS)
            if(Platform.os != OS.MacOS) {
                val oldContext = GLContext.current()
                val tmpContext = GLContext.create()
                tmpContext.makeCurrent()

                // Get info
                val extensions = tmpContext.getExtensions()
                hasWGLNVInteropExt = tmpContext.hasFunction("wglDXOpenDeviceNV") && tmpContext.hasFunction("wglDXLockObjectsNV")
                hasMemoryObjectExt = "GL_EXT_memory_object" in extensions && ("GL_EXT_memory_object_win32" in extensions || "GL_EXT_memory_object_fd" in extensions)

                // Clear temporary context
                tmpContext.delete()
                oldContext.makeCurrent()
            }

            val type = when (Platform.os) {
                OS.Windows -> {
                    if(pipeline == "d3d" && hasMemoryObjectExt && isDXGISupported())
                        ExternalObjectsD3D
                    else if(pipeline == "es2" && hasMemoryObjectExt)
                        ExternalObjectsESWin
                    else if(pipeline == "d3d" && hasWGLNVInteropExt)
                        WGLDXInterop
                    else
                        Blit
                }

                OS.Linux -> {
                    if (pipeline == "es2" && hasMemoryObjectExt)
                        ExternalObjectsESLinux
                    else
                        Blit
                }

                OS.MacOS -> {
                    if(pipeline == "es2")
                        IOSurface
                    else
                        Blit
                }
                OS.Other -> throw UnsupportedOperationException("Unsupported OS")
            }
            type
        }

        /**
         * Check if `glImportMemoryWin32HandleEXT` works, so we can use this interop
         */
        private fun isDXGISupported(): Boolean{
            // Create GL context and D3D9 device
            val oldContext = GLContext.current()
            val tmpContext = GLContext.create()
            tmpContext.makeCurrent()
            GLExecutor.loadBasicFunctionPointers()
            val d3d9 = D3D9.Device()

            // Simulate real interop process
            val texture = d3d9.createTexture(10, 10)
            val memoryObj = glCreateMemoryObjectsEXT()
            glImportMemoryWin32HandleEXT(memoryObj, 0, GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT, texture.sharedHandle)

            val sharedTexture = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, sharedTexture)
            glTextureStorageMem2DEXT(sharedTexture, 1, GL_BGRA, 10, 10, memoryObj, 0)

            // Check for errors
            val supported = glGetError() == 0

            // Release
            texture.release()
            tmpContext.delete()

            oldContext.makeCurrent()
            return supported
        }
    }
}