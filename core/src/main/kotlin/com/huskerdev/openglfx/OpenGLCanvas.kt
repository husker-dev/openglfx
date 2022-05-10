package com.huskerdev.openglfx


import com.huskerdev.openglfx.events.GLInitializeEvent
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import com.huskerdev.openglfx.utils.OpenGLFXUtils
import com.huskerdev.openglfx.utils.RegionAccessorObject
import com.huskerdev.openglfx.utils.RegionAccessorOverrider

import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import javafx.scene.layout.Pane
import java.util.function.Consumer

enum class DirectDrawPolicy {
    NEVER,
    IF_AVAILABLE,
    ALWAYS
}

abstract class OpenGLCanvas: Pane() {

    companion object{
        init {
            RegionAccessorOverrider.overwrite(object: RegionAccessorObject<OpenGLCanvas>(){
                override fun doCreatePeer(node: OpenGLCanvas) = NGOpenGLCanvas(node)
            })
        }

        @JvmOverloads
        @JvmStatic
        fun create(
            initializer: FXGLInitializer,
            directDrawPolicy: DirectDrawPolicy = DirectDrawPolicy.NEVER
        ): OpenGLCanvas {
            val isES2 = OpenGLFXUtils.pipelineName == "es2"
            return when(directDrawPolicy) {
                DirectDrawPolicy.NEVER -> {
                    if(!initializer.supportsUniversal)
                        throw UnsupportedOperationException("${initializer.name} doesn't support universal rendering")
                    initializer.createUniversal()
                }
                DirectDrawPolicy.ALWAYS -> {
                    if(!initializer.supportsDirect)
                        throw UnsupportedOperationException("${initializer.name} doesn't support direct rendering")
                    if(!isES2)
                        throw UnsupportedOperationException("Direct rendering only supports ES2 JavaFX pipeline (current: ${OpenGLFXUtils.pipelineName})")
                    initializer.createDirect()
                }
                DirectDrawPolicy.IF_AVAILABLE -> {
                    if(initializer.supportsDirect && isES2) initializer.createDirect()
                    else initializer.createUniversal()
                }
            }
        }
    }

    private var onInit = arrayListOf<Consumer<GLInitializeEvent>>()
    private var onRender = arrayListOf<Consumer<GLRenderEvent>>()
    private var onReshape = arrayListOf<Consumer<GLReshapeEvent>>()
    private var onDispose = arrayListOf<Runnable>()

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

    fun onDispose(listener: Runnable){
        onDispose.add(listener)
    }

    protected abstract fun onNGRender(g: Graphics)
    abstract fun repaint()

    protected open fun fireRenderEvent() {
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

        val event = GLRenderEvent(GLRenderEvent.ANY, currentFps, delta)

        onRender.forEach { it.accept(event) }
    }

    protected open fun fireReshapeEvent(width: Int, height: Int) {
        checkInitialization()

        val event = GLReshapeEvent(GLReshapeEvent.ANY, width, height)
        onReshape.forEach { it.accept(event) }
    }

    protected open fun fireInitEvent() = checkInitialization()

    protected open fun fireDisposeEvent() {
        checkInitialization()
        onDispose.forEach { it.run() }
    }

    private fun checkInitialization(){
        if(!initialized){
            initialized = true

            val event = GLInitializeEvent(GLInitializeEvent.ANY)
            onInit.forEach { it.accept(event) }
        }
    }

    private class NGOpenGLCanvas(val canvas: OpenGLCanvas): NGRegion() {

        override fun renderContent(g: Graphics) {
            canvas.onNGRender(g)
            super.renderContent(g)
        }
    }
}