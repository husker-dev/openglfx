package com.huskerdev.openglfx.jogl

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import com.huskerdev.openglfx.canvas.implementations.NVDXInteropCanvasImpl
import com.huskerdev.openglfx.canvas.implementations.SharedCanvasImpl
import com.huskerdev.openglfx.canvas.implementations.BlitCanvasImpl
import com.huskerdev.openglfx.jogl.events.JOGLDisposeEvent
import com.huskerdev.openglfx.jogl.events.JOGLInitializeEvent
import com.huskerdev.openglfx.jogl.events.JOGLRenderEvent
import com.huskerdev.openglfx.jogl.events.JOGLReshapeEvent
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLProfile
import jogamp.opengl.GLDrawableFactoryImpl

class JOGLUniversalCanvas(
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

class JOGLInteropCanvas(
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