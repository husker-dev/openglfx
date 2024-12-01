package com.huskerdev.openglfx.internal

import com.huskerdev.grapl.core.platform.OS
import com.huskerdev.grapl.gl.GLContext
import com.sun.prism.GraphicsPipeline

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
    ExternalObjectsWinD3D,

    /**
     *  Creates shared object between two GL context using Vulkan proxy
     *
     *  - Supported platforms: **Windows**
     */
    ExternalObjectsWinES,

    /**
     *  Creates shared object between two GL context using Vulkan proxy
     *
     *  - Supported platforms: **Linux**
     */
    ExternalObjectsFd,

    /**
    *  Creates memory block in VRAM that can be used in different OpenGL contexts.
    *
    *  - Supported platforms: **macOS**
    */
    IOSurface;

    companion object {
        val auto: GLInteropType by lazy {
            val pipeline = GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]
            val tmpContext = GLContext.create()
            tmpContext.makeCurrent()
            val extensions = tmpContext.getExtensions()

            val type = when (com.huskerdev.grapl.core.platform.Platform.os) {
                OS.Windows -> {
                    if("GL_EXT_memory_object" in extensions && "GL_EXT_memory_object_win32" in extensions && (pipeline == "d3d" || pipeline == "es2")){
                        if(pipeline == "d3d")
                            ExternalObjectsWinD3D
                        else
                            ExternalObjectsWinES
                    } else if (pipeline == "d3d" && tmpContext.hasFunction("wglDXOpenDeviceNV") && tmpContext.hasFunction("wglDXLockObjectsNV"))
                        WGLDXInterop
                    else
                        Blit
                }

                OS.Linux -> {
                    if (pipeline == "es2" && "GL_EXT_memory_object" in extensions && "GL_EXT_memory_object_fd" in extensions)
                        ExternalObjectsFd
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
            tmpContext.delete()
            type
        }
    }
}