package com.huskerdev.openglfx.internal.platforms.macos

import com.huskerdev.grapl.gl.GLContext
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GL_BGRA
import com.huskerdev.openglfx.GL_RGBA
import com.huskerdev.openglfx.GL_TEXTURE_RECTANGLE
import com.huskerdev.openglfx.GL_UNSIGNED_INT_8_8_8_8_REV

internal class IOSurface(val width: Int, val height: Int) {

    companion object {
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

    fun createBoundTexture(): Int {
        val texture = glGenTextures()
        glBindTexture(GL_TEXTURE_RECTANGLE, texture)
        nCGLTexImageIOSurface2D(
            GLContext.current().handle,
            GL_TEXTURE_RECTANGLE,
            GL_RGBA,
            width, height,
            GL_BGRA,
            GL_UNSIGNED_INT_8_8_8_8_REV,
            handle, 0)
        return texture
    }

    fun lock() = nIOSurfaceLock(handle)

    fun unlock() = nIOSurfaceUnlock(handle)

    fun dispose() = nReleaseIOSurface(handle)
}