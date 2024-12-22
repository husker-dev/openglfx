package com.huskerdev.openglfx.jme

import com.huskerdev.openglfx.jme.internal.OGLFXSurfaceContext
import com.jme3.app.SimpleApplication
import com.jme3.app.state.AppState
import com.jme3.system.AppSettings

abstract class SimpleFXApplication(
    vararg initialStates: AppState
): SimpleApplication(*initialStates) {

    init {
        setSettings(AppSettings(true).apply {
            setCustomRenderer(OGLFXSurfaceContext::class.java)
            isResizable = true
        })
        createCanvas()
    }

    override fun start() {
        val canvasContext = getContext() as OGLFXSurfaceContext
        //canvasContext.setApplication(this)
        canvasContext.systemListener = this
        startCanvas(true)
    }

}