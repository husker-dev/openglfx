package com.huskerdev.openglfx


import com.huskerdev.openglfx.utils.FXUtils
import com.huskerdev.openglfx.utils.RegionAccessorObject
import com.huskerdev.openglfx.utils.RegionAccessorOverrider

import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import javafx.scene.layout.Pane

enum class DirectDrawPolicy {
    NEVER,
    IF_AVAILABLE,
    ALWAYS
}

abstract class OpenGLCanvas: Pane() {

    companion object{
        init{
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
            val isES2 = FXUtils.pipelineName == "es2"
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
                        throw UnsupportedOperationException("Direct rendering only supports ES2 JavaFX pipeline (current: ${FXUtils.pipelineName})")
                    initializer.createDirect()
                }
                DirectDrawPolicy.IF_AVAILABLE -> {
                    if(initializer.supportsDirect && isES2) initializer.createDirect()
                    else initializer.createUniversal()
                }
            }
        }
    }

    private var onInit: Runnable? = null
    private var onRender: Runnable? = null
    private var onUpdate: Runnable? = null
    private var onReshape: Runnable? = null
    private var onDispose: Runnable? = null

    private var initialized = false

    protected val dpi: Double
        get() = scene.window.outputScaleX

    protected val scaledWidth: Double
        get() = width * dpi

    protected val scaledHeight: Double
        get() = height * dpi

    fun onRender(listener: Runnable){
        onRender = listener
    }

    fun onUpdate(listener: Runnable){
        onUpdate = listener
    }

    fun onReshape(listener: Runnable){
        onReshape = listener
    }

    fun onInitialize(listener: Runnable){
        onInit = listener
        initialized = false
    }

    fun onDispose(listener: Runnable){
        onDispose = listener
    }

    fun createTimer(fps: Double, applier: (FXGLTimer) -> Unit = {}): FXGLTimer{
        return FXGLTimer(fps)
            .apply(applier)
            .apply {
                canvas = this@OpenGLCanvas
                started = true
            }
    }

    protected abstract fun onNGRender(g: Graphics)
    protected abstract fun requestRepaint()

    fun repaint(){
        onUpdate?.run()
        requestRepaint()
    }

    protected open fun fireRenderEvent() {
        checkInitialization()
        onRender?.run()
    }

    protected open fun fireReshapeEvent() {
        checkInitialization()
        onReshape?.run()
    }

    protected open fun fireInitEvent() = checkInitialization()

    protected open fun fireDisposeEvent() {
        checkInitialization()
        onDispose?.run()
    }

    private fun checkInitialization(){
        if(!initialized){
            initialized = true
            onInit?.run()
        }
    }

    private class NGOpenGLCanvas(val canvas: OpenGLCanvas): NGRegion() {

        override fun renderContent(g: Graphics) {
            canvas.onNGRender(g)
            super.renderContent(g)
        }
    }
}