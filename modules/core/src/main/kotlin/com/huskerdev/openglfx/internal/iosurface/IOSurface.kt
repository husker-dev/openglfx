package com.huskerdev.openglfx.internal.iosurface

import com.huskerdev.ojgl.GLContext

internal class IOSurface(val width: Int, val height: Int) {

    companion object {
        @JvmStatic external fun nGetDisplayDPI(x: Double , y: Double): Double

        @JvmStatic private external fun nCreateIOSurface(width: Int, height: Int): Long
        @JvmStatic private external fun nReleaseIOSurface(ioSurfaceRef: Long)
        @JvmStatic private external fun nCGLTexImageIOSurface2D(
            ctx: Long, target: Int, internalFormat: Int,
            width: Int, height: Int, format: Int, type: Int,
            ioSurfaceRef: Long, plane: Int): Int
        @JvmStatic private external fun nIOSurfaceLock(ioSurfaceRef: Long)
        @JvmStatic private external fun nIOSurfaceUnlock(ioSurfaceRef: Long)
    }

    private val handle = nCreateIOSurface(width, height)

    fun cglTexImageIOSurface2D(ctx: GLContext, target: Int, internalFormat: Int,
                               format: Int, type: Int,
                               plane: Int): Int =
        nCGLTexImageIOSurface2D(ctx.handle, target, internalFormat, width, height, format, type, handle, plane)

    fun lock() = nIOSurfaceLock(handle)

    fun unlock() = nIOSurfaceUnlock(handle)

    fun dispose() = nReleaseIOSurface(handle)
}