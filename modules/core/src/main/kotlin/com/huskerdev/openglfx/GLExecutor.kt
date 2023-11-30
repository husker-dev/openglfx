package com.huskerdev.openglfx

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import com.huskerdev.openglfx.internal.canvas.NVDXInteropCanvasImpl
import com.huskerdev.openglfx.internal.canvas.SharedCanvasImpl
import com.huskerdev.openglfx.internal.canvas.BlitCanvasImpl
import com.huskerdev.openglfx.internal.canvas.IOSurfaceCanvasImpl
import com.huskerdev.openglfx.internal.canvas.async.AsyncBlitCanvasImpl
import com.huskerdev.openglfx.internal.canvas.async.AsyncIOSurfaceCanvasImpl
import com.huskerdev.openglfx.internal.canvas.async.AsyncNVDXInteropCanvasImpl
import com.huskerdev.openglfx.internal.canvas.async.AsyncSharedCanvasImpl
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


internal const val GL_BGRA = 0x80E1
internal const val GL_RGBA = 0x1908
internal const val GL_RGBA8 = 0x8058
internal const val GL_UNSIGNED_BYTE = 0x1401
internal const val GL_UNSIGNED_INT_8_8_8_8_REV = 0x8367
internal const val GL_FRAMEBUFFER = 0x8D40
internal const val GL_TEXTURE_2D = 0xDE1
internal const val GL_TEXTURE_RECTANGLE = 0x84F5
internal const val GL_RENDERBUFFER = 0x8D41
internal const val GL_COLOR_ATTACHMENT0 = 0x8CE0
internal const val GL_DEPTH_COMPONENT = 0x1902
internal const val GL_DEPTH_ATTACHMENT = 0x8D00
internal const val GL_TEXTURE_MIN_FILTER = 0x2801
internal const val GL_NEAREST = 0x2600
internal const val GL_READ_FRAMEBUFFER = 0x8CA8
internal const val GL_DRAW_FRAMEBUFFER = 0x8CA9
internal const val GL_COLOR_BUFFER_BIT = 0x4000
internal const val GL_DRAW_FRAMEBUFFER_BINDING = 0x8CA6
internal const val GL_READ_FRAMEBUFFER_BINDING = 0x8CAA
internal const val GL_MAX_SAMPLES = 0x8D57
internal const val GL_VERTEX_SHADER = 0x8B31
internal const val GL_FRAGMENT_SHADER = 0x8B30
internal const val GL_ARRAY_BUFFER = 0x8892
internal const val GL_FLOAT = 0x1406
internal const val GL_STATIC_DRAW = 0x88E4
internal const val GL_TRIANGLE_STRIP = 0x0005


open class GLExecutor {

    companion object {

        @JvmStatic val NONE_MODULE = object: GLExecutor(){}

        private var isInitialized = false
        @JvmStatic external fun nInitGLFunctions()

        @JvmStatic external fun glDeleteFramebuffers(fbo: Int)
        @JvmStatic external fun glDeleteRenderbuffers(rbo: Int)
        @JvmStatic external fun glDeleteTextures(texture: Int)
        @JvmStatic external fun glGenFramebuffers(): Int
        @JvmStatic external fun glGenRenderbuffers(): Int
        @JvmStatic external fun glGenTextures(): Int
        @JvmStatic external fun glBindFramebuffer(target: Int, fbo: Int)
        @JvmStatic external fun glBindRenderbuffer(target: Int, rbo: Int)
        @JvmStatic external fun glBindTexture(target: Int, texture: Int)
        @JvmStatic external fun glFramebufferTexture2D(target: Int, attachment: Int, texture: Int, texId: Int, level: Int)
        @JvmStatic external fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)
        @JvmStatic external fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbufferTarget: Int, renderbuffer: Int)
        @JvmStatic external fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: ByteBuffer)
        @JvmStatic external fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: ByteBuffer?)
        @JvmStatic external fun glTexParameteri(target: Int, pname: Int, param: Int)
        @JvmStatic external fun glViewport(x: Int, y: Int, w: Int, h: Int)
        @JvmStatic external fun glFinish()

        @JvmStatic external fun glRenderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int)
        @JvmStatic external fun glBlitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: Int, filter: Int)
        @JvmStatic external fun glGetInteger(pname: Int): Int

        // Shaders
        @JvmStatic external fun glCreateShader(type: Int): Int
        @JvmStatic external fun glDeleteShader(shader: Int)
        @JvmStatic external fun glShaderSource(shader: Int, source: String)
        @JvmStatic external fun glCompileShader(shader: Int)
        @JvmStatic external fun glCreateProgram(): Int
        @JvmStatic external fun glAttachShader(program: Int, shader: Int)
        @JvmStatic external fun glLinkProgram(program: Int)
        @JvmStatic external fun glUseProgram(program: Int)
        @JvmStatic external fun glGetUniformLocation(program: Int, name: String): Int
        @JvmStatic external fun glUniform2f(location: Int, value1: Float, value2: Float)

        // Buffers
        @JvmStatic external fun glGenVertexArrays(): Int
        @JvmStatic external fun glBindVertexArray(vao: Int)
        @JvmStatic external fun glGenBuffers(): Int
        @JvmStatic external fun glBindBuffer(target: Int, buffer: Int)
        @JvmStatic external fun glBufferData(target: Int, vertices: FloatBuffer, type: Int)
        @JvmStatic external fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long)
        @JvmStatic external fun glEnableVertexAttribArray(index: Int)
        @JvmStatic external fun glDeleteBuffers(buffer: Int)
        @JvmStatic external fun glDrawArrays(mode: Int, first: Int, count: Int)

        internal fun floatBuffer(array: FloatArray): FloatBuffer {
            return ByteBuffer.allocateDirect(array.size * Float.SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().put(array)
        }

        internal fun loadBasicFunctionPointers(){
            if(isInitialized) return
            isInitialized = true
            if(GLContext.current().handle == 0L) {
                val context = GLContext.create()
                context.makeCurrent()
                nInitGLFunctions()
                GLContext.delete(context)
            } else nInitGLFunctions()
        }
    }

    open fun blitNGCanvas(canvas: GLCanvas, executor: GLExecutor, profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        if(async) AsyncBlitCanvasImpl(canvas, executor, profile, flipY, msaa)
        else BlitCanvasImpl(canvas, executor, profile, flipY, msaa)

    open fun sharedNGCanvas(canvas: GLCanvas, executor: GLExecutor, profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        if(async) AsyncSharedCanvasImpl(canvas, executor, profile, flipY, msaa)
        else SharedCanvasImpl(canvas, executor, profile, flipY, msaa)

    open fun interopNGCanvas(canvas: GLCanvas, executor: GLExecutor, profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        if(async) AsyncNVDXInteropCanvasImpl(canvas, executor, profile, flipY, msaa)
        else NVDXInteropCanvasImpl(canvas, executor, profile, flipY, msaa)

    open fun ioSurfaceNGCanvas(canvas: GLCanvas, executor: GLExecutor, profile: GLProfile, flipY: Boolean, msaa: Int, async: Boolean) =
        if(async) AsyncIOSurfaceCanvasImpl(canvas, executor, profile, flipY, msaa)
        else IOSurfaceCanvasImpl(canvas, executor, profile, flipY, msaa)

    open fun createRenderEvent(canvas: GLCanvas, currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int)
            = GLRenderEvent(GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    open fun createReshapeEvent(canvas: GLCanvas, width: Int, height: Int)
            = GLReshapeEvent(GLReshapeEvent.ANY, width, height)
    open fun createInitEvent(canvas: GLCanvas)
            = GLInitializeEvent(GLInitializeEvent.ANY)
    open fun createDisposeEvent(canvas: GLCanvas)
            = GLDisposeEvent(GLDisposeEvent.ANY)

    open fun initGLFunctions() = loadBasicFunctionPointers()

}