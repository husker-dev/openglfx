package com.huskerdev.openglfx.lwjgl

import com.huskerdev.openglfx.core.GLExecutor
import org.lwjgl.opengl.*
import java.nio.IntBuffer


class LWJGLExecutor: GLExecutor() {

    companion object {
        @JvmField val LWJGL_MODULE = LWJGLExecutor()
    }

    override fun initGLFunctions() {
        GL.createCapabilities()
    }

    // GL
    override fun glDeleteFramebuffers(fbo: Int) = GL30.glDeleteFramebuffers(fbo)
    override fun glDeleteRenderbuffers(rbo: Int) = GL30.glDeleteRenderbuffers(rbo)
    override fun glDeleteTextures(texture: Int) = GL30.glDeleteTextures(texture)

    override fun glGenFramebuffers() = GL30.glGenFramebuffers()
    override fun glGenRenderbuffers() = GL30.glGenRenderbuffers()
    override fun glGenTextures() = GL30.glGenTextures()

    override fun glBindFramebuffer(target: Int, fbo: Int) = GL30.glBindFramebuffer(target, fbo)
    override fun glBindRenderbuffer(target: Int, rbo: Int) = GL30.glBindRenderbuffer(target, rbo)
    override fun glBindTexture(target: Int, texture: Int) = GL30.glBindTexture(target, texture)

    override fun glFramebufferTexture2D(target: Int, attachment: Int, texture: Int, texId: Int, level: Int) = GL30.glFramebufferTexture2D(target, attachment, texture, texId, level)
    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) = GL30.glRenderbufferStorage(target, internalformat, width, height)
    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbufferTarget: Int, renderbuffer: Int) = GL30.glFramebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer)

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IntBuffer) = GL30.glReadPixels(x, y, width, height, format, type, pixels)
    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Long)
        = GL30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels)
    override fun glTexParameteri(target: Int, pname: Int, param: Int) = GL30.glTexParameteri(target, pname, param)

    override fun glViewport(x: Int, y: Int, w: Int, h: Int) = GL30.glViewport(x, y, w, h)
    override fun glFinish() = GL30.glFinish()
}