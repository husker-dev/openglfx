package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.core.GLExecutor
import com.huskerdev.openglfx.core.NativeObject
import com.jogamp.common.nio.Buffers
import com.jogamp.common.nio.PointerBuffer
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLDrawableFactory
import com.jogamp.opengl.GLProfile
import jogamp.opengl.GLContextImpl
import jogamp.opengl.macosx.cgl.CGL
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

@JvmField
val JOGL_MODULE = JOGLExecutor()

class JOGLExecutor: GLExecutor() {


    private lateinit var gl: GL2
    private val buffer = intArrayOf(0)

    override fun universalCanvas(profile: Int) = JOGLUniversalCanvas(this, profile)
    override fun sharedCanvas(profile: Int) = JOGLSharedCanvas(this, profile)
    override fun interopCanvas(profile: Int) = JOGLInteropCanvas(this, profile)

    private fun createPointer(ptr: Long): PointerBuffer{
        val buffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())

        val address = Buffer::class.java.getDeclaredField("address")
        address.isAccessible = true
        val capacity = Buffer::class.java.getDeclaredField("capacity")
        capacity.isAccessible = true

        address.setLong(buffer, ptr)
        capacity.setInt(buffer, Long.SIZE_BYTES)

        return PointerBuffer.wrap(buffer)
    }

    // Invokes only when context is currently bound to the Thread
    override fun initGLFunctions() {
        if(::gl.isInitialized)
            return
        val context = GLDrawableFactory.getFactory(GLProfile.getDefault()).createExternalGLContext() as GLContextImpl
        gl = context.gl.gL2
    }

    override fun createNativeObject() = object: NativeObject() {
        val pointer = PointerBuffer.allocateDirect(Long.SIZE_BYTES)

        val addressField = Buffer::class.java.getDeclaredField("address").apply { isAccessible = true }

        override val value: Long
            get() = pointer.get()
        override val address: Long
            get() = addressField.getLong(pointer.buffer)
    }

    // GL
    override fun glDeleteFramebuffers(fbo: Int) = gl.glDeleteFramebuffers(1, intArrayOf(fbo), 0)
    override fun glDeleteRenderbuffers(rbo: Int) = gl.glDeleteRenderbuffers(1, intArrayOf(rbo), 0)
    override fun glDeleteTextures(texture: Int) = gl.glDeleteTextures(1, intArrayOf(texture), 0)

    override fun glGenFramebuffers(): Int {
        gl.glGenFramebuffers(1, buffer, 0)
        return buffer[0]
    }

    override fun glGenRenderbuffers(): Int {
        gl.glGenRenderbuffers(1, buffer, 0)
        return buffer[0]
    }

    override fun glGenTextures(): Int {
        gl.glGenTextures(1, buffer, 0)
        return buffer[0]
    }

    override fun glBindFramebuffer(target: Int, fbo: Int) = gl.glBindFramebuffer(target, fbo)
    override fun glBindRenderbuffer(target: Int, rbo: Int) = gl.glBindRenderbuffer(target, rbo)
    override fun glBindTexture(target: Int, texture: Int) = gl.glBindTexture(target, texture)

    override fun glFramebufferTexture2D(target: Int, attachment: Int, texture: Int, texId: Int, level: Int) = gl.glFramebufferTexture2D(target, attachment, texture, texId, level)
    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) = gl.glRenderbufferStorage(target, internalformat, width, height)
    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbufferTarget: Int, renderbuffer: Int) = gl.glFramebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer)

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: IntBuffer) = gl.glReadPixels(x, y, width, height, format, type, pixels)
    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Long)
        = gl.glTexImage2D(target, level, internalformat, width, height, border, format, type, null)
    override fun glTexParameteri(target: Int, pname: Int, param: Int) = gl.glTexParameteri(target, pname, param)

    override fun glViewport(x: Int, y: Int, w: Int, h: Int) = gl.glViewport(x, y, w, h)
    override fun glFinish() = gl.glFinish()

    // CGL
    override fun CGLGetCurrentContext() = CGL.CGLGetCurrentContext()
    override fun CGLSetCurrentContext(context: Long) = CGL.CGLSetCurrentContext(context)
    override fun CGLGetPixelFormat(context: Long) = CGL.CGLGetPixelFormat(context)
    override fun CGLCreateContext(pix: Long, share: Long, ctxPtr: Long) = CGL.CGLCreateContext(pix, share, createPointer(ctxPtr))
    override fun CGLDestroyPixelFormat(pix: Long) = CGL.CGLDestroyPixelFormat(pix)
    override fun CGLChoosePixelFormat(attribs: IntArray, pixPtr: Long, npix: IntArray): Int {
        val buffer = Buffers.newDirectIntBuffer(1)
        val result = CGL.CGLChoosePixelFormat(Buffers.newDirectIntBuffer(attribs), createPointer(pixPtr), buffer)
        npix[0] = buffer.get()
        println("Result: $result, npix: ${npix[0]}")
        return result
    }

}