package com.husker.openglfx

import com.husker.openglfx.gl.DirectGLRenderer
import com.husker.openglfx.universal.UniversalRenderer
import com.husker.openglfx.utils.FXUtils
import com.husker.openglfx.utils.NodeUtils
import com.husker.openglfx.utils.RegionAccessorRewriter
import com.jogamp.opengl.GL
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import javafx.animation.AnimationTimer
import javafx.scene.layout.Pane


abstract class OpenGLCanvas protected constructor(
    val capabilities: GLCapabilities
): Pane() {

    companion object{
        init{
            RegionAccessorRewriter.rewrite<OpenGLCanvas> { NGGLCanvas(it) }
        }

        @JvmOverloads
        @JvmStatic
        fun create(capabilities: GLCapabilities = GLCapabilities(GLProfile.getDefault()), targetFPS: Int = 60, requireDirectDraw: Boolean = true): OpenGLCanvas{
            return if(requireDirectDraw && FXUtils.pipelineName == "es2")
                DirectGLRenderer(capabilities)
            else UniversalRenderer(capabilities, targetFPS)
        }
    }

    private val eventListeners = arrayListOf<FXGLEventListener>()
    private val initializedListener = arrayListOf<FXGLEventListener>()

    protected val dpi: Double
        get() = scene.window.outputScaleX

    protected val scaledWidth: Double
        get() = width * dpi

    protected val scaledHeight: Double
        get() = height * dpi

    fun addFXGLEventListener(listener: FXGLEventListener){
        eventListeners.add(listener)
    }

    fun addFXGLEventListener(index: Int, listener: FXGLEventListener){
        eventListeners.add(index, listener)
    }

    fun removeFXGLEventListener(listener: FXGLEventListener){
        initializedListener.remove(listener)
    }

    abstract fun onRender(g: Graphics)

    protected fun fireDisplayEvent(gl: GL) = eventListeners.forEach {
        checkListenerInitialization(gl, it)
        it.display(gl)
    }

    protected fun fireReshapeEvent(gl: GL) = eventListeners.forEach {
        checkListenerInitialization(gl, it)
        it.reshape(gl, scaledWidth.toFloat(), scaledHeight.toFloat())
    }

    protected fun fireInitEvent(gl: GL) = eventListeners.forEach {
        checkListenerInitialization(gl, it)
        it.init(gl)
    }

    protected fun fireDisposeEvent(gl: GL) = eventListeners.forEach {
        checkListenerInitialization(gl, it)
        it.dispose(gl)
    }

    private fun checkListenerInitialization(gl: GL, listener: FXGLEventListener){
        if(listener !in initializedListener){
            initializedListener.add(listener)
            listener.init(gl)
        }
    }

    private class NGGLCanvas(val canvas: OpenGLCanvas): NGRegion() {

        init{
            NodeUtils.onWindowReady(canvas){
                object: AnimationTimer(){
                    override fun handle(p: Long) {
                        NodeHelper.markDirty(canvas, DirtyBits.NODE_GEOMETRY)
                    }
                }.start()
            }
        }

        override fun renderContent(g: Graphics) {
            canvas.onRender(g)
            super.renderContent(g)
        }
    }
}