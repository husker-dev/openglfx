package com.huskerdev.openglfx.internal

import com.huskerdev.ojgl.utils.OS
import com.huskerdev.ojgl.utils.PlatformUtils
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
    NVDXInterop,

    /**
     *  Creates context that is shared with JavaFX's one. After rendering, shared texture is displayed in JavaFX frame.
     *
     *  - Supported platforms: **Linux**
     */
    TextureSharing,

    /**
    *  Creates memory block in VRAM that can be used in different OpenGL contexts.
    *
    *  - Supported platforms: **macOS**
    */
    IOSurface;

    companion object {
        val supported: GLInteropType
            get() = when (GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]) {
                "es2" -> if(PlatformUtils.os == OS.MacOS) IOSurface else TextureSharing
                "d3d" -> if (com.huskerdev.openglfx.internal.d3d9.NVDXInterop.isSupported()) NVDXInterop else Blit
                else -> Blit
            }
    }
}