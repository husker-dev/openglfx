package com.huskerdev.openglfx.core

import com.huskerdev.openglfx.core.implementations.*
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


abstract class GLExecutor {

    open fun universalCanvas(profile: Int) = UniversalImpl(this, profile)
    open fun sharedCanvas(profile: Int) = SharedImpl(this, profile)
    open fun interopCanvas(profile: Int) = InteropImpl(this, profile)

    abstract fun initGLFunctions()

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
}