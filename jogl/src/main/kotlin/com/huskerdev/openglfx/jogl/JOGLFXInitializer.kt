package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.core.GLExecutor
import com.huskerdev.openglfx.core.NativeObject
import com.huskerdev.openglfx.core.impl.InteropGLCanvas
import com.huskerdev.openglfx.core.impl.SharedGLCanvas
import com.huskerdev.openglfx.core.impl.UniversalGLCanvas
import com.jogamp.common.nio.Buffers
import com.jogamp.common.nio.PointerBuffer
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import jogamp.opengl.GLContextImpl
import jogamp.opengl.GLDrawableFactoryImpl
import jogamp.opengl.GLOffscreenAutoDrawableImpl
import jogamp.opengl.gl4.GL4bcImpl
import jogamp.opengl.macosx.cgl.CGL
import jogamp.opengl.windows.wgl.WGL
import jogamp.opengl.windows.wgl.WGLExtImpl
import jogamp.opengl.windows.wgl.WindowsWGLContext
import jogamp.opengl.x11.glx.GLX
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

@JvmField
val JOGL_MODULE = JOGLExecutor()

class JOGLExecutor: GLExecutor() {

    companion object {
        private lateinit var gl: GL2
        private lateinit var wglExt: WGLExtImpl
        private var isGLHooked = false

        private fun applyGL(gl: GL2){
            this.gl = gl
            val context = gl.context

            if(context is WindowsWGLContext)
                wglExt = WGLExtImpl(context)

            isGLHooked = true
        }
    }

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

    // Can be invoked in any time
    private fun <T> checkContext(run: () -> T): T{
        if(!isGLHooked){
            isGLHooked = true

            val drawable = GLDrawableFactoryImpl
                .getFactoryImpl(GLProfile.getDefault())
                .createOffscreenAutoDrawable(GLProfile.getDefaultDevice(), GLCapabilities(GLProfile.getDefault()), null, 100, 100) as GLOffscreenAutoDrawableImpl
            drawable.display()
            applyGL(drawable.gl.gL2)
        }
        return run()
    }

    // Invokes only when context is currently bound to the Thread
    override fun initGLFunctions() {
        if(isGLHooked)
            return
        applyGL(GL4bcImpl(
            GLProfile.getDefault(),
            GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext() as GLContextImpl
        ))
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

    // WGL
    override fun wglGetCurrentContext() = checkContext { WGL.wglGetCurrentContext() }
    override fun wglGetCurrentDC() = checkContext { WGL.wglGetCurrentDC() }
    override fun wglMakeCurrent(dc: Long, context: Long) = checkContext { WGL.wglMakeCurrent(dc, context) }
    override fun wglCreateContext(dc: Long) = checkContext { WGL.wglCreateContext(dc) }
    override fun wglShareLists(rc1: Long, rc2: Long) = checkContext { WGL.wglShareLists(rc1, rc2) }

    // WGL DX Interop
    override fun wglDXOpenDeviceNV(dxDevice: Long) = wglExt.wglDXOpenDeviceNV(createPointer(dxDevice).buffer)
    override fun wglDXRegisterObjectNV(device: Long, dxResource: Long, name: Int, type: Int, access: Int) = wglExt.wglDXRegisterObjectNV(device, createPointer(dxResource).buffer, name, type, access)
    override fun wglDXSetResourceShareHandleNV(dxObject: Long, shareHandle: Long) = wglExt.wglDXSetResourceShareHandleNV(createPointer(dxObject).buffer, shareHandle)
    override fun wglDXUnregisterObjectNV(device: Long, obj: Long) = wglExt.wglDXUnregisterObjectNV(device, obj)

    override fun hasWGLDX() = checkContext { (gl.context as WindowsWGLContext).wglExtProcAddressTable.isFunctionAvailable("wglDXOpenDeviceNV") }
    override fun getWglDXLockObjectsNVPtr() = checkContext { (gl.context as WindowsWGLContext).wglExtProcAddressTable.getAddressFor("wglDXLockObjectsNV") }
    override fun getWglDXUnlockObjectsNVPtr() = checkContext { (gl.context as WindowsWGLContext).wglExtProcAddressTable.getAddressFor("wglDXUnlockObjectsNV") }

    // CGL
    override fun CGLGetCurrentContext() = CGL.CGLGetCurrentContext()
    override fun CGLSetCurrentContext(context: Long) = CGL.CGLSetCurrentContext(context)
    override fun CGLGetPixelFormat(context: Long) = CGL.CGLGetPixelFormat(context)
    override fun CGLCreateContext(pix: Long, share: Long, ctxPtr: Long) = CGL.CGLCreateContext(pix, share, createPointer(ctxPtr))
    override fun CGLDestroyPixelFormat(pix: Long) = CGL.CGLDestroyPixelFormat(pix)
    override fun CGLChoosePixelFormat(attribs: IntArray, pixPtr: Long, npix: IntArray) = CGL.CGLChoosePixelFormat(Buffers.newDirectIntBuffer(attribs), createPointer(pixPtr), Buffers.newDirectIntBuffer(npix))

    // GLX
    override fun glXGetCurrentDisplay() = GLX.glXGetCurrentDisplay()
    override fun glXGetCurrentContext() = GLX.glXGetCurrentContext()
    override fun glXMakeCurrent(display: Long, draw: Long, ctx: Long) = GLX.glXMakeCurrent(display, draw, ctx)

}