package com.huskerdev.openglfx.lwjgl

import com.huskerdev.openglfx.core.GLExecutor
import com.huskerdev.openglfx.core.NativeObject
import org.lwjgl.PointerBuffer
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.windows.GDI32.ChoosePixelFormat
import org.lwjgl.system.windows.GDI32.SetPixelFormat
import org.lwjgl.system.windows.PIXELFORMATDESCRIPTOR
import org.lwjgl.system.windows.User32.*
import org.lwjgl.system.windows.WNDCLASSEX
import org.lwjgl.system.windows.WinBase.nGetModuleHandle
import java.nio.IntBuffer

@JvmField
val LWJGL_MODULE = LWJGLExecutor()

class LWJGLExecutor: GLExecutor() {

    override fun initGLFunctions() {
        GL.createCapabilities()
    }

    override fun createNativeObject() = object: NativeObject(){
        val pointer = PointerBuffer.allocateDirect(Long.SIZE_BYTES)

        override val value: Long
            get() = pointer.get()
        override val address: Long
            get() = pointer.address()
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

    // WGL
    override fun wglGetCurrentContext() = WGL.wglGetCurrentContext()
    override fun wglGetCurrentDC() = WGL.wglGetCurrentDC()
    override fun wglMakeCurrent(dc: Long, context: Long) = WGL.wglMakeCurrent(dc, context)
    override fun wglCreateContext(dc: Long) = WGL.wglCreateContext(dc)
    override fun wglShareLists(rc1: Long, rc2: Long) = WGL.wglShareLists(rc1, rc2)

    // WGL DX Interop
    override fun wglDXOpenDeviceNV(dxDevice: Long) = WGLNVDXInterop.wglDXOpenDeviceNV(dxDevice)
    override fun wglDXRegisterObjectNV(device: Long, dxResource: Long, name: Int, type: Int, access: Int) = WGLNVDXInterop.wglDXRegisterObjectNV(device, dxResource, name, type, access)
    override fun wglDXSetResourceShareHandleNV(dxObject: Long, shareHandle: Long) = WGLNVDXInterop.wglDXSetResourceShareHandleNV(dxObject, shareHandle)
    override fun wglDXUnregisterObjectNV(device: Long, obj: Long) = WGLNVDXInterop.wglDXUnregisterObjectNV(device, obj)

    override fun hasWGLDX() = GL.getCapabilitiesWGL().wglDXOpenDeviceNV != 0L
    override fun getWglDXLockObjectsNVPtr() = GL.getCapabilitiesWGL().wglDXLockObjectsNV
    override fun getWglDXUnlockObjectsNVPtr() = GL.getCapabilitiesWGL().wglDXUnlockObjectsNV

    // CGL
    override fun CGLGetCurrentContext() = CGL.CGLGetCurrentContext()
    override fun CGLSetCurrentContext(context: Long) = CGL.CGLSetCurrentContext(context)
    override fun CGLGetPixelFormat(context: Long) = CGL.CGLGetPixelFormat(context)
    override fun CGLCreateContext(pix: Long, share: Long, ctxPtr: Long) = CGL.CGLCreateContext(pix, share, PointerBuffer.create(ctxPtr, Long.SIZE_BYTES))
    override fun CGLDestroyPixelFormat(pix: Long) = CGL.CGLDestroyPixelFormat(pix)
    override fun CGLChoosePixelFormat(attribs: IntArray, pixPtr: Long, npix: IntArray) = CGL.CGLChoosePixelFormat(attribs, PointerBuffer.create(pixPtr, Long.SIZE_BYTES), npix)

    // GLX
    override fun glXGetCurrentDisplay() = GLX12.glXGetCurrentDisplay()
    override fun glXGetCurrentContext() = GLX12.glXGetCurrentContext()
    override fun glXMakeCurrent(display: Long, draw: Long, ctx: Long) = GLX12.glXMakeCurrent(display, draw, ctx)
}