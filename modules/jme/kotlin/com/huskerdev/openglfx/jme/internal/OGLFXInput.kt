package com.huskerdev.openglfx.jme.internal

import com.jme3.input.Input
import com.jme3.input.RawInputListener
import javafx.scene.Node

abstract class OGLFXInput(
    val context: OGLFXSurfaceContext
): Input {

    private var initialized = false

    protected var listener: RawInputListener? = null
    protected var node: Node? = null

    open fun bind(node: Node) {
        this.node = node
    }

    open fun unbind() {
        this.node = null
    }

    override fun initialize() {
        initialized = true
    }

    override fun destroy() {
        unbind()
    }

    override fun isInitialized() =
        initialized

    override fun setInputListener(listener: RawInputListener) {
        this.listener = listener
    }

    override fun getInputTimeNanos() =
        System.nanoTime()

}