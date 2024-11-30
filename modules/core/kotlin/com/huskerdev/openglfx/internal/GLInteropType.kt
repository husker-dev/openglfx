package com.huskerdev.openglfx.internal

enum class GLInteropType {
    AUTO,

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
     *  Creates shared object between GL and DirectX
     *
     *  - Supported platforms: **Windows**
     */
    SharedObjectsWin32,

    /**
     *  Creates shared object between two GL context
     *
     *  - Supported platforms: **Linux**
     */
    SharedObjectsFd,

    /**
    *  Creates memory block in VRAM that can be used in different OpenGL contexts.
    *
    *  - Supported platforms: **macOS**
    */
    IOSurface;
}