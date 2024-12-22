package com.huskerdev.openglfx.jme.internal

import com.huskerdev.openglfx.canvas.GLCanvas
import com.jme3.input.JoyInput
import com.jme3.input.KeyInput
import com.jme3.input.MouseInput
import com.jme3.input.TouchInput
import com.jme3.opencl.Context
import com.jme3.renderer.Renderer
import com.jme3.system.AppSettings
import com.jme3.system.JmeContext
import com.jme3.system.JmeSystem
import com.jme3.system.SystemListener
import com.jme3.system.Timer
import kotlin.math.max

class OGLFXSurfaceContext(
    val canvas: GLCanvas
): JmeContext {

    private val settings = AppSettings(true).apply {
        renderer = AppSettings.LWJGL_OPENGL32
    }

    private val keyInput = OGLFXKeyInput(this)
    private val mouseInput = OGLFXMouseInput(this)

    private val backgroundContext = JmeSystem.newContext(settings, JmeContext.Type.OffscreenSurface)

    init {
    }

    override fun getType() =
        JmeContext.Type.OffscreenSurface

    override fun setSettings(settings: AppSettings?) {
        this.settings.copyFrom(settings)
        this.settings.renderer = AppSettings.LWJGL_OPENGL32

        backgroundContext.settings = settings
    }

    override fun getSystemListener(): SystemListener =
        backgroundContext.systemListener

    override fun setSystemListener(listener: SystemListener) {
        backgroundContext.systemListener = listener
    }

    override fun getSettings(): AppSettings =
        settings

    override fun getRenderer(): Renderer? =
        backgroundContext.renderer

    override fun getOpenCLContext(): Context? =
        null

    override fun getMouseInput(): MouseInput? =
        mouseInput

    override fun getKeyInput(): KeyInput? =
        keyInput

    override fun getJoyInput(): JoyInput? =
        null

    override fun getTouchInput(): TouchInput? =
        null

    override fun getTimer(): Timer? =
        backgroundContext.timer

    override fun setTitle(title: String?) = Unit

    override fun isCreated(): Boolean =
        backgroundContext != null && backgroundContext.isCreated

    override fun isRenderable(): Boolean =
        backgroundContext != null && backgroundContext.isRenderable

    override fun setAutoFlushFrames(enabled: Boolean) = Unit

    override fun create(waitFor: Boolean) {
        val backgroundContext = backgroundContext
        //backgroundContext.settings.renderer = render
        backgroundContext.create(waitFor)
    }

    override fun restart() = Unit

    override fun destroy(waitFor: Boolean) {
        backgroundContext.destroy(waitFor)
    }

    override fun getFramebufferHeight(): Int =
        max(1, canvas.scaledWidth)

    override fun getFramebufferWidth(): Int =
        max(1, canvas.scaledHeight)

    override fun getWindowXPosition(): Int = 0

    override fun getWindowYPosition(): Int = 0
}