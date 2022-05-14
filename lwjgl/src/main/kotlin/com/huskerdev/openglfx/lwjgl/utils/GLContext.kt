package com.huskerdev.openglfx.lwjgl.utils

import com.sun.javafx.PlatformUtil
import org.lwjgl.PointerBuffer
import org.lwjgl.opengl.CGL.*
import org.lwjgl.opengl.GLX13.*
import org.lwjgl.opengl.WGL.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.linux.X11.XDefaultScreen
import org.lwjgl.system.windows.GDI32.ChoosePixelFormat
import org.lwjgl.system.windows.GDI32.SetPixelFormat
import org.lwjgl.system.windows.PIXELFORMATDESCRIPTOR
import org.lwjgl.system.windows.User32.*
import org.lwjgl.system.windows.WNDCLASSEX
import org.lwjgl.system.windows.WinBase.nGetModuleHandle


abstract class GLContext {

    companion object {

        fun createNew(): GLContext {
            if(PlatformUtil.isWindows()){
                val stack = MemoryStack.stackGet()
                val stackPointer = stack.pointer
                try{
                    val className = stack.UTF16Safe("openglfx", true)!!

                    RegisterClassEx(WNDCLASSEX.create()
                        .cbSize(WNDCLASSEX.SIZEOF)
                        .lpfnWndProc { hwnd, uMsg, wParam, lParam -> DefWindowProc(hwnd, uMsg, wParam, lParam) }
                        .hInstance(nGetModuleHandle(0))
                        .lpszClassName(className)
                    )

                    val hwnd = CreateWindowEx(
                        WS_EX_LAYERED,
                        className, className,
                        WS_OVERLAPPEDWINDOW,
                        0, 0,
                        100, 100,
                        0, 0, 0, 0
                    )

                    val dc = GetDC(hwnd)
                    val pfd = PIXELFORMATDESCRIPTOR.create().nSize(PIXELFORMATDESCRIPTOR.SIZEOF.toShort())
                    SetPixelFormat(dc, ChoosePixelFormat(dc, pfd), pfd)
                    val rc = wglCreateContext(dc)

                    return WGLContext(rc, dc)
                } finally {
                    stack.pointer = stackPointer
                }
            }
            if(PlatformUtil.isMac()){
                val pix = PointerBuffer.allocateDirect(1)
                val num = intArrayOf(0)
                val context = PointerBuffer.allocateDirect(1)

                CGLChoosePixelFormat(intArrayOf(kCGLPFAAccelerated, kCGLPFAOpenGLProfile, kCGLOGLPVersion_Legacy, 0), pix, num)
                CGLCreateContext(pix.address(), 0, context)
                CGLDestroyPixelFormat(pix.address())

                return CGLContext(context.address())
            }

            throw UnsupportedOperationException("Unsupported OS")
        }

        fun createNew(shareWith: GLContext): GLContext {
            if(PlatformUtil.isWindows()){
                shareWith as WGLContext
                val rc = wglCreateContext(shareWith.dc)
                wglShareLists(shareWith.rc, rc)

                return WGLContext(rc, shareWith.dc)
            }
            if(PlatformUtil.isMac()){
                shareWith as CGLContext
                val pix = PointerBuffer.allocateDirect(1)
                val num = intArrayOf(1)
                val context = PointerBuffer.allocateDirect(1)

                println("CGLChoosePixelFormat: " + CGLChoosePixelFormat(intArrayOf(kCGLPFAAccelerated, kCGLPFAOpenGLProfile, kCGLOGLPVersion_3_2_Core, 0), pix, num))
                println("pix: $pix")
                println("pix.address: ${pix.address()}")
                println("pix.limit: ${pix.limit()}")
                println("pix.position: ${pix.position()}")
                println("num: ${num[0]}")
                println("pix.get: ${pix.get()}")
                CGLCreateContext(pix.get(), shareWith.context, context)
                CGLDestroyPixelFormat(pix.get())

                return CGLContext(context.get())
            }
            if(PlatformUtil.isLinux()){
                shareWith as GLXContext

                val vsInfo = glXChooseVisual(shareWith.display, XDefaultScreen(shareWith.display), intArrayOf(
                    GLX_RGBA,
                    GLX_RED_SIZE,		8,
                    GLX_GREEN_SIZE, 	8,
                    GLX_BLUE_SIZE,		8,
                    GLX_ALPHA_SIZE, 	8,
                    GLX_DOUBLEBUFFER,
                    GLX_DEPTH_SIZE,		24,
                    GLX_STENCIL_SIZE,	8,
                    0
                ))

                val context = glXCreateContext(shareWith.display, vsInfo!!, shareWith.context, true)
                return GLXContext(shareWith.display, context)
            }

            throw UnsupportedOperationException("Unsupported OS")
        }

        fun fromCurrent() =
            if(PlatformUtil.isWindows())
                WGLContext(wglGetCurrentContext(), wglGetCurrentDC())
            else if(PlatformUtil.isMac())
                CGLContext(CGLGetCurrentContext())
            else if(PlatformUtil.isLinux())
                GLXContext(glXGetCurrentDisplay(), glXGetCurrentContext())
            else throw UnsupportedOperationException("Unsupported OS")

        fun clearCurrent(){
            if(PlatformUtil.isWindows())
                wglMakeCurrent(0, 0)
            else if(PlatformUtil.isMac())
                CGLSetCurrentContext(0)
            else if(PlatformUtil.isLinux())
                glXMakeCurrent(0, 0, 0)
            else throw UnsupportedOperationException("Unsupported OS")
        }

    }

    abstract fun makeCurrent()

    class WGLContext(
        val rc: Long,
        val dc: Long
    ): GLContext() {

        override fun makeCurrent() {
            wglMakeCurrent(dc, rc)
        }
    }

    class CGLContext(
        val context: Long,
    ): GLContext() {

        override fun makeCurrent() {
            CGLSetCurrentContext(context)
        }
    }

    class GLXContext(
        val display: Long,
        val context: Long,
    ): GLContext() {

        override fun makeCurrent() {
            glXMakeCurrent(display, 0, context)
        }
    }
}