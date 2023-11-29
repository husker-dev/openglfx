package com.huskerdev.openglfx.internal

import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLProfile
import com.sun.javafx.sg.prism.NGNode
import com.sun.javafx.sg.prism.NGRegion
import com.sun.prism.Graphics
import com.sun.prism.Texture
import javafx.animation.AnimationTimer

abstract class NGGLCanvas(
    val canvas: GLCanvas,
    val executor: GLExecutor,
    val profile: GLProfile,
    val flipY: Boolean,
    val msaa: Int
): NGRegion(
) {

    @Volatile var disposed = false
        private set

    val width by canvas::width
    val height by canvas::height
    val scaledWidth by canvas::scaledWidth
    val scaledHeight by canvas::scaledHeight
    internal val scaledSize: Size
        get() = Size(scaledWidth, scaledHeight)

    private val animationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            timerTick()
        }
    }.apply { start() }


    override fun setParent(parent: NGNode?) {
        super.setParent(parent)
        if(canvas.scene != null)
            canvas.fireSceneBoundEvent()
    }

    /*
    protected fun markDirty(){
        NodeHelper.markDirty(canvas, DirtyBits.NODE_BOUNDS)
        NodeHelper.markDirty(canvas, DirtyBits.REGION_SHAPE)
    }
     */

    protected abstract fun timerTick()
    abstract fun repaint()

    /**
     * Fills node by texture
     *
     * @param g Node's graphics
     * @param texture default JavaFX texture
     */
    protected fun drawResultTexture(g: Graphics, texture: Texture){
        if(disposed) return
        if(flipY) g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0f, 0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        else      g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0f, scaledHeight.toFloat(), scaledWidth.toFloat(), 0f)
    }

    open fun dispose(){
        animationTimer.stop()
        disposed = true
    }
}