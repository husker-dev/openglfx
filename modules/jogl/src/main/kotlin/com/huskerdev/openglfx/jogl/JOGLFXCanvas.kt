package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import com.huskerdev.openglfx.internal.canvas.NVDXInteropCanvasImpl
import com.huskerdev.openglfx.internal.canvas.SharedCanvasImpl
import com.huskerdev.openglfx.internal.canvas.BlitCanvasImpl
import com.huskerdev.openglfx.internal.canvas.async.AsyncBlitCanvasImpl
import com.huskerdev.openglfx.internal.canvas.async.AsyncNVDXInteropCanvasImpl
import com.huskerdev.openglfx.internal.canvas.async.AsyncSharedCanvasImpl
import com.huskerdev.openglfx.jogl.events.JOGLDisposeEvent
import com.huskerdev.openglfx.jogl.events.JOGLInitializeEvent
import com.huskerdev.openglfx.jogl.events.JOGLRenderEvent
import com.huskerdev.openglfx.jogl.events.JOGLReshapeEvent
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLProfile
import jogamp.opengl.GLDrawableFactoryImpl

class JOGLBlitCanvas(
    executor: GLExecutor,
    profile: com.huskerdev.openglfx.canvas.GLProfile,
    flipY: Boolean,
    msaa: Int
): BlitCanvasImpl(executor, profile, flipY, msaa){
    val gl: GL3 by lazy {
        GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL3
    }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int) =
        JOGLRenderEvent(gl, GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    override fun createReshapeEvent(width: Int, height: Int) =
        JOGLReshapeEvent(gl, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() =
        JOGLInitializeEvent(gl, GLInitializeEvent.ANY)
    override fun createDisposeEvent() =
        JOGLDisposeEvent(gl, GLDisposeEvent.ANY)
}

class JOGLAsyncBlitCanvas(
    executor: GLExecutor,
    profile: com.huskerdev.openglfx.canvas.GLProfile,
    flipY: Boolean,
    msaa: Int
): AsyncBlitCanvasImpl(executor, profile, flipY, msaa){
    val gl: GL3 by lazy {
        GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL3
    }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int) =
        JOGLRenderEvent(gl, GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    override fun createReshapeEvent(width: Int, height: Int) =
        JOGLReshapeEvent(gl, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() =
        JOGLInitializeEvent(gl, GLInitializeEvent.ANY)
    override fun createDisposeEvent() =
        JOGLDisposeEvent(gl, GLDisposeEvent.ANY)
}

class JOGLSharedCanvas(
    executor: GLExecutor,
    profile: com.huskerdev.openglfx.canvas.GLProfile,
    flipY: Boolean,
    msaa: Int
): SharedCanvasImpl(executor, profile, flipY, msaa){
    val gl: GL3 by lazy {
        GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL3
    }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int) =
        JOGLRenderEvent(gl, GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    override fun createReshapeEvent(width: Int, height: Int) =
        JOGLReshapeEvent(gl, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() =
        JOGLInitializeEvent(gl, GLInitializeEvent.ANY)
    override fun createDisposeEvent() =
        JOGLDisposeEvent(gl, GLDisposeEvent.ANY)
}

class JOGLAsyncSharedCanvas(
    executor: GLExecutor,
    profile: com.huskerdev.openglfx.canvas.GLProfile,
    flipY: Boolean,
    msaa: Int
): AsyncSharedCanvasImpl(executor, profile, flipY, msaa){
    val gl: GL3 by lazy {
        GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL3
    }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int) =
        JOGLRenderEvent(gl, GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    override fun createReshapeEvent(width: Int, height: Int) =
        JOGLReshapeEvent(gl, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() =
        JOGLInitializeEvent(gl, GLInitializeEvent.ANY)
    override fun createDisposeEvent() =
        JOGLDisposeEvent(gl, GLDisposeEvent.ANY)
}

class JOGLNVDXInteropCanvas(
    executor: GLExecutor,
    profile: com.huskerdev.openglfx.canvas.GLProfile,
    flipY: Boolean,
    msaa: Int
): NVDXInteropCanvasImpl(executor, profile, flipY, msaa){
    val gl: GL3 by lazy {
        GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL3
    }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int) =
        JOGLRenderEvent(gl, GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    override fun createReshapeEvent(width: Int, height: Int) =
        JOGLReshapeEvent(gl, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() =
        JOGLInitializeEvent(gl, GLInitializeEvent.ANY)
    override fun createDisposeEvent() =
        JOGLDisposeEvent(gl, GLDisposeEvent.ANY)
}

class JOGLAsyncNVDXInteropCanvas(
    executor: GLExecutor,
    profile: com.huskerdev.openglfx.canvas.GLProfile,
    flipY: Boolean,
    msaa: Int
): AsyncNVDXInteropCanvasImpl(executor, profile, flipY, msaa){
    val gl: GL3 by lazy {
        GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault()).createExternalGLContext().gl.gL3
    }

    override fun createRenderEvent(currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int) =
        JOGLRenderEvent(gl, GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    override fun createReshapeEvent(width: Int, height: Int) =
        JOGLReshapeEvent(gl, GLReshapeEvent.ANY, width, height)
    override fun createInitEvent() =
        JOGLInitializeEvent(gl, GLInitializeEvent.ANY)
    override fun createDisposeEvent() =
        JOGLDisposeEvent(gl, GLDisposeEvent.ANY)
}