package com.huskerdev.openglfx.core

import com.huskerdev.openglfx.core.impl.*
import java.nio.IntBuffer


const val GL_BGRA = 0x80E1
const val GL_RGBA = 0x1908
const val GL_UNSIGNED_BYTE = 0x1401
const val GL_UNSIGNED_INT_8_8_8_8_REV = 0x8367
const val GL_FRAMEBUFFER = 0x8D40
const val GL_TEXTURE_2D = 0xDE1
const val GL_RENDERBUFFER = 0x8D41
const val GL_COLOR_ATTACHMENT0 = 0x8CE0
const val GL_DEPTH_COMPONENT = 0x1902
const val GL_DEPTH_ATTACHMENT = 0x8D00
const val GL_TEXTURE_MIN_FILTER = 0x2801
const val GL_NEAREST = 0x2600

const val kCGLPFAAccelerated = 73
const val kCGLPFAOpenGLProfile = 99
const val kCGLOGLPVersion_Legacy = 0x1000
const val kCGLOGLPVersion_3_2_Core = 0x3200


abstract class GLExecutor {

    open fun universalCanvas(profile: Int) = UniversalGLCanvas(this, profile)
    open fun sharedCanvas(profile: Int) = SharedGLCanvas(this, profile)
    open fun interopCanvas(profile: Int) = InteropGLCanvas(this, profile)

    abstract fun initGLFunctions()
    abstract fun createNativeObject(): NativeObject

    // GL
    abstract fun glDeleteFramebuffers(fbo: Int)
    abstract fun glDeleteRenderbuffers(rbo: Int)
    abstract fun glDeleteTextures(texture: Int)
    abstract fun glGenFramebuffers(): Int
    abstract fun glGenRenderbuffers(): Int
    abstract fun glGenTextures(): Int
    abstract fun glBindFramebuffer(target: Int, fbo: Int)
    abstract fun glBindRenderbuffer(target: Int, rbo: Int)
    abstract fun glBindTexture(target: Int, texture: Int)
    abstract fun glFramebufferTexture2D(target: Int, attachment: Int, texture: Int, texId: Int, level: Int)
    abstract fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)
    abstract fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbufferTarget: Int, renderbuffer: Int)
    abstract fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IntBuffer)
    abstract fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Long)
    abstract fun glTexParameteri(target: Int, pname: Int, param: Int)

    abstract fun glViewport(x: Int, y: Int, w: Int, h: Int)
    abstract fun glFinish()

    // CGL
    abstract fun CGLGetCurrentContext(): Long
    abstract fun CGLSetCurrentContext(context: Long): Int
    abstract fun CGLGetPixelFormat(context: Long): Long
    abstract fun CGLCreateContext(pix: Long, share: Long, ctxPtr: Long): Int
    abstract fun CGLDestroyPixelFormat(pix: Long): Int
    abstract fun CGLChoosePixelFormat(attribs: IntArray, pixPtr: Long, npix: IntArray): Int
}

abstract class NativeObject {
    abstract val value: Long
    abstract val address: Long
}