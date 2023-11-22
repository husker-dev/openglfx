package com.huskerdev.openglfx.internal

import com.huskerdev.openglfx.canvas.GLCanvas
import com.sun.javafx.sg.prism.NGNode
import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import javafx.scene.Scene

internal class NGGLCanvas(
    val canvas: GLCanvas,
    val renderCallback: (Graphics) -> Unit
): NGRegion() {
    private val sceneBoundListeners = arrayListOf<(Scene) -> Unit>()
    private val preRenderListeners = arrayListOf<() -> Unit>()
    private val postRenderListeners = arrayListOf<() -> Unit>()

    override fun renderContent(g: Graphics) {
        if(!canvas.disposed) {
            if (preRenderListeners.size > 0)
                preRenderListeners.forEach { it() }
            renderCallback(g)
            if (postRenderListeners.size > 0)
                postRenderListeners.forEach { it() }
        }
        super.renderContent(g)
    }

    override fun setParent(parent: NGNode?) {
        super.setParent(parent)
        if(canvas.scene != null){
            while(sceneBoundListeners.size > 0)
                sceneBoundListeners.removeLast()(canvas.scene)
        }
    }

    fun addSceneConnectedListener(listener: (Scene) -> Unit){
        if(canvas.scene != null) listener(canvas.scene)
        else sceneBoundListeners.add(listener)
    }

    fun addPreRenderListener(listener: () -> Unit){
        preRenderListeners.add(listener)
    }

    fun addPostRenderListener(listener: () -> Unit){
        postRenderListeners.add(listener)
    }
}