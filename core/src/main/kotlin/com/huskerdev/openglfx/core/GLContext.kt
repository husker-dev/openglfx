package com.huskerdev.openglfx.core

import com.huskerdev.openglfx.CORE_PROFILE
import com.huskerdev.openglfx.utils.WinUtils
import com.sun.javafx.PlatformUtil

abstract class GLContext(
    val executor: GLExecutor
) {

    companion object {
        fun createNew(executor: GLExecutor, profile: Int): GLContext = executor.run {
            if(PlatformUtil.isWindows()){
                val result = WinUtils.createGLContext(
                    profile == CORE_PROFILE, 0L,
                    executor.getWglChoosePixelFormatARBPtr(), executor.getWglCreateContextAttribsARBPtr())

                return WGLContext(executor, result[0], result[1])
            }
            if(PlatformUtil.isMac()){
                val pix = executor.createNativeObject()
                val num = intArrayOf(1)
                val context = executor.createNativeObject()

                val version = if(profile == CORE_PROFILE) kCGLOGLPVersion_3_2_Core else kCGLOGLPVersion_Legacy
                CGLChoosePixelFormat(intArrayOf(kCGLPFAAccelerated, kCGLPFAOpenGLProfile, version, 0), pix.address, num)

                val pixelFormat = pix.value
                CGLCreateContext(pixelFormat, 0, context.address)
                CGLDestroyPixelFormat(pixelFormat)

                return CGLContext(executor, context.value)
            }
            throw UnsupportedOperationException("Unsupported OS")
        }

        fun createNew(executor: GLExecutor, profile: Int, shareWith: GLContext): GLContext = executor.run {
            if(PlatformUtil.isWindows()){
                val result = WinUtils.createGLContext(
                    profile == CORE_PROFILE, (shareWith as WGLContext).rc,
                    executor.getWglChoosePixelFormatARBPtr(), executor.getWglCreateContextAttribsARBPtr())

                return WGLContext(executor, result[0], result[1])
            }
            if(PlatformUtil.isMac()){
                shareWith as CGLContext
                val pix = executor.createNativeObject()
                val num = intArrayOf(1)
                val context = executor.createNativeObject()

                val version = if(profile == CORE_PROFILE) kCGLOGLPVersion_3_2_Core else kCGLOGLPVersion_Legacy
                CGLChoosePixelFormat(intArrayOf(kCGLPFAAccelerated, kCGLPFAOpenGLProfile, version, 0), pix.address, num)

                val pixelFormat = pix.value
                CGLCreateContext(pixelFormat, shareWith.context, context.address)
                CGLDestroyPixelFormat(pixelFormat)

                return CGLContext(executor, context.value)
            }
            throw UnsupportedOperationException("Unsupported OS")
        }

        fun fromCurrent(executor: GLExecutor) = executor.run {
            if (PlatformUtil.isWindows())
                WGLContext(executor, wglGetCurrentContext(), wglGetCurrentDC())
            else if (PlatformUtil.isMac())
                CGLContext(executor, CGLGetCurrentContext())
            else if (PlatformUtil.isLinux())
                GLXContext(executor, glXGetCurrentDisplay(), glXGetCurrentContext())
            else throw UnsupportedOperationException("Unsupported OS")
        }

        fun clearCurrent(executor: GLExecutor) = executor.run {
            if(PlatformUtil.isWindows())
                wglMakeCurrent(0, 0)
            else if(PlatformUtil.isMac())
                CGLSetCurrentContext(0) == 0
            else if(PlatformUtil.isLinux())
                glXMakeCurrent(0, 0, 0)
            else throw UnsupportedOperationException("Unsupported OS")
        }
    }

    abstract fun makeCurrent(): Boolean

    class WGLContext(
        executor: GLExecutor,
        val rc: Long,
        val dc: Long
    ): GLContext(executor) {
        override fun makeCurrent() = executor.wglMakeCurrent(dc, rc)
    }

    class CGLContext(
        executor: GLExecutor,
        val context: Long,
    ): GLContext(executor) {
        override fun makeCurrent() = executor.CGLSetCurrentContext(context) == 0
    }

    class GLXContext(
        executor: GLExecutor,
        val display: Long,
        val context: Long,
    ): GLContext(executor) {
        override fun makeCurrent() = executor.glXMakeCurrent(display, 0, context)
    }
}