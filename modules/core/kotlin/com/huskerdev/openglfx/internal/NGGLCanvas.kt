package com.huskerdev.openglfx.internal

import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.GLCanvas
import com.sun.javafx.geom.BaseBounds
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.sg.prism.NGNode
import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import javafx.application.Platform
import kotlin.math.max

abstract class NGGLCanvas(
    val canvas: GLCanvas,
    val executor: GLExecutor,
    val profile: GLProfile
): NGRegion(
) {
    @Volatile var disposed = false
        private set

    val width by canvas::width
    val height by canvas::height
    val scaledWidth by canvas::scaledWidth
    val scaledHeight by canvas::scaledHeight
    internal val scaledSize: Size
        get() = Size(max(1, scaledWidth), max(1, scaledHeight))

    val flipY by canvas::flipY
    val async by canvas::async
    val msaa by canvas::msaa
    val fxaa by canvas::fxaa

    private val animationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            timerTick()
        }
    }.apply { start() }

    init {
        Platform.runLater(::repaint)
    }

    protected abstract fun timerTick()
    abstract fun repaint()

    override fun setParent(parent: NGNode?) {
        super.setParent(parent)
        if(canvas.scene != null)
            canvas.fireSceneBoundEvent()
    }

    override fun setTransformedBounds(bounds: BaseBounds?, byTransformChangeOnly: Boolean) {
        super.setTransformedBounds(bounds, byTransformChangeOnly)
        repaint()
    }

    override fun setVisible(value: Boolean) {
        super.setVisible(value)
        repaint()
    }

    protected fun dirty(){
        NodeHelper.markDirty(canvas, DirtyBits.NODE_BOUNDS)
        NodeHelper.markDirty(canvas, DirtyBits.REGION_SHAPE)
    }

    protected fun drawResultTexture(g: Graphics, texture: Texture){
        if(disposed) return
        val drawWidth = (texture.physicalWidth / canvas.dpi).toFloat()
        val drawHeight = (texture.physicalHeight / canvas.dpi).toFloat()
        val sourceWidth = texture.physicalWidth.toFloat()
        val sourceHeight = texture.physicalHeight.toFloat()

        if(flipY) g.drawTexture(texture, 0f, 0f, drawWidth, drawHeight, 0f, 0f, sourceWidth, sourceHeight)
        else      g.drawTexture(texture, 0f, 0f, drawWidth, drawHeight, 0f, sourceHeight, sourceWidth, 0f)
    }

    open fun dispose(){
        animationTimer.stop()
        disposed = true
    }
}