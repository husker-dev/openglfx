package com.huskerdev.openglfx.jogl

import com.huskerdev.grapl.gl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import com.huskerdev.openglfx.jogl.events.JOGLDisposeEvent
import com.huskerdev.openglfx.jogl.events.JOGLInitializeEvent
import com.huskerdev.openglfx.jogl.events.JOGLRenderEvent
import com.huskerdev.openglfx.jogl.events.JOGLReshapeEvent
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLProfile
import jogamp.opengl.GLDrawableFactoryImpl

@JvmField @Suppress("unused")
val JOGL_MODULE = JOGLExecutor()

class JOGLExecutor: GLExecutor() {

    private val gl = hashMapOf<GLCanvas, GL3>()

    override fun createRenderEvent(canvas: GLCanvas, currentFps: Int, delta: Double, width: Int, height: Int, fbo: Int): GLRenderEvent {
        checkInit(canvas)
        return JOGLRenderEvent(gl[canvas]!!, GLRenderEvent.ANY, currentFps, delta, width, height, fbo)
    }

    override fun createReshapeEvent(canvas: GLCanvas, width: Int, height: Int): GLReshapeEvent {
        checkInit(canvas)
        return JOGLReshapeEvent(gl[canvas]!!, GLReshapeEvent.ANY, width, height)
    }

    override fun createInitEvent(canvas: GLCanvas): GLInitializeEvent {
        checkInit(canvas)
        return JOGLInitializeEvent(gl[canvas]!!, GLInitializeEvent.ANY)
    }

    override fun createDisposeEvent(canvas: GLCanvas): GLDisposeEvent {
        val gl = gl.remove(canvas)
        return JOGLDisposeEvent(gl!!, GLDisposeEvent.ANY)
    }

    private fun checkInit(canvas: GLCanvas){
        if(!gl.containsKey(canvas)){
            // https://github.com/husker-dev/openglfx/issues/22
            // 'java.awt.headless' is used to prevent lock on macOS, when JOGL
            // tries to initialize using AWT classes
            val originalHeadless = System.getProperty("java.awt.headless")
            val ctx = GLContext.current()

            System.setProperty("java.awt.headless", "true")
            val factory = GLDrawableFactoryImpl.getFactory(GLProfile.getDefaultDevice())
            ctx.makeCurrent()
            gl[canvas] = factory.createExternalGLContext().gl.gL3
            if(originalHeadless != null)
                System.setProperty("java.awt.headless", originalHeadless)
        }
    }
}