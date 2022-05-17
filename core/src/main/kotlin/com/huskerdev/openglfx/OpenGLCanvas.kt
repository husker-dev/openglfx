package com.huskerdev.openglfx


import com.huskerdev.openglfx.core.GLExecutor
import com.huskerdev.openglfx.core.impl.InteropGLCanvas
import com.huskerdev.openglfx.core.impl.SharedGLCanvas
import com.huskerdev.openglfx.core.impl.UniversalGLCanvas
import com.huskerdev.openglfx.events.GLDisposeEvent
import com.huskerdev.openglfx.events.GLInitializeEvent
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import com.huskerdev.openglfx.utils.OpenGLFXUtils
import com.huskerdev.openglfx.utils.RegionAccessorObject
import com.huskerdev.openglfx.utils.RegionAccessorOverrider

import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import com.sun.prism.Texture
import javafx.scene.layout.Pane
import java.util.function.Consumer


abstract class OpenGLCanvas: Pane() {

    companion object {

        var forceUniversal = false

        init {
            RegionAccessorOverrider.overwrite(object : RegionAccessorObject<OpenGLCanvas>() {
                override fun doCreatePeer(node: OpenGLCanvas) = NGOpenGLCanvas(node)
            })
        }

        @JvmStatic
        fun create(executor: GLExecutor): OpenGLCanvas {
            return if(forceUniversal)
                executor.universalCanvas
            else when (OpenGLFXUtils.pipelineName) {
                "es2" -> executor.sharedCanvas
                "d3d" -> if(executor.hasWGLDX())
                    executor.interopCanvas else executor.universalCanvas
                else -> executor.universalCanvas
            }
        }
    }

    private var onInit = arrayListOf<Consumer<GLInitializeEvent>>()
    private var onRender = arrayListOf<Consumer<GLRenderEvent>>()
    private var onReshape = arrayListOf<Consumer<GLReshapeEvent>>()
    private var onDispose = arrayListOf<Consumer<GLDisposeEvent>>()

    private var initialized = false

    // Delta
    private var lastDeltaTime = System.nanoTime()

    // Fps
    private var lastFpsTime = System.nanoTime()
    private var countedFps = 0
    private var currentFps = 0

    var animator: GLCanvasAnimator? = null
        set(value) {
            if(field != null) {
                field!!.started = false
                field!!.boundCanvas = null
            }

            if(value != null)
                value.boundCanvas = this

            field = value
        }

    protected val dpi: Double
        get() {
            if(scene == null || scene.window == null)
                return 1.0
            return scene.window.outputScaleX
        }

    protected val scaledWidth: Double
        get() = width * dpi

    protected val scaledHeight: Double
        get() = height * dpi

    fun onRender(listener: Consumer<GLRenderEvent>){
        onRender.add(listener)
    }

    fun onReshape(listener: Consumer<GLReshapeEvent>){
        onReshape.add(listener)
    }

    fun onInitialize(listener: Consumer<GLInitializeEvent>){
        onInit.add(listener)
        initialized = false
    }

    fun onDispose(listener: Consumer<GLDisposeEvent>){
        onDispose.add(listener)
    }

    protected abstract fun onNGRender(g: Graphics)
    abstract fun repaint()

    protected fun fireRenderEvent() {
        checkInitialization()

        val now = System.nanoTime()
        val delta = (now - lastDeltaTime) / 1000000000.0
        lastDeltaTime = now

        countedFps++
        if((now - lastFpsTime) / 1000000 > 1000) {
            currentFps = countedFps
            countedFps = 0
            lastFpsTime = now
        }

        dispatchRenderEvent(GLRenderEvent(GLRenderEvent.ANY, currentFps, delta))
    }

    protected fun fireReshapeEvent(width: Int, height: Int) {
        checkInitialization()
        dispatchReshapeEvent(GLReshapeEvent(GLReshapeEvent.ANY, width, height))
    }

    protected fun fireInitEvent() = checkInitialization()
    protected fun fireDisposeEvent() = dispatchDisposeEvent(GLDisposeEvent(GLDisposeEvent.ANY))

    private fun checkInitialization(){
        if(!initialized){
            initialized = true
            dispatchInitEvent(GLInitializeEvent(GLInitializeEvent.ANY))
        }
    }

    protected open fun dispatchRenderEvent(event: GLRenderEvent) = onRender.forEach { it.accept(event) }
    protected open fun dispatchReshapeEvent(event: GLReshapeEvent) = onReshape.forEach { it.accept(event) }
    protected open fun dispatchInitEvent(event: GLInitializeEvent) = onInit.forEach { it.accept(event) }
    protected open fun dispatchDisposeEvent(event: GLDisposeEvent) = onDispose.forEach { it.accept(event) }

    protected fun drawResultTexture(g: Graphics, texture: Texture){
        if(!texture.isLocked)
            texture.lock()
        g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        texture.unlock()
    }

    private class NGOpenGLCanvas(val canvas: OpenGLCanvas): NGRegion() {

        override fun renderContent(g: Graphics) {
            canvas.onNGRender(g)
            super.renderContent(g)
        }
    }
}