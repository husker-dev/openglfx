package com.huskerdev.openglfx.lwjgl.shared

import com.huskerdev.openglfx.lwjgl.LWJGLCanvas
import com.huskerdev.openglfx.lwjgl.utils.GLContext
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.RTTexture
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import java.util.concurrent.atomic.AtomicBoolean

class LWJGLShared: LWJGLCanvas() {

    private var lastSize = Pair(-1, -1)
    private var initialized = false

    private var context: GLContext? = null
    private var fxContext: GLContext? = null

    private var texture = -1
    private var fbo = -1
    private var depthBuffer = -1

    private var fxTexture: RTTexture? = null

    private var needsRepaint = AtomicBoolean(false)

    init {
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                try {
                    if(needsRepaint.getAndSet(false)) {
                        NodeHelper.markDirty(this@LWJGLShared, DirtyBits.NODE_BOUNDS)
                        NodeHelper.markDirty(this@LWJGLShared, DirtyBits.REGION_SHAPE)
                    }
                } catch (_: Exception){}
            }
        }.start()
    }

    private fun updateFramebufferSize() {
        if(fbo != -1)
            glDeleteFramebuffers(fbo)
        if(depthBuffer != -1)
            glDeleteRenderbuffers(depthBuffer)

        fxTexture?.dispose()
        fxTexture = GraphicsPipeline.getDefaultResourceFactory().createRTTexture(lastSize.first, lastSize.second, Texture.WrapMode.CLAMP_TO_ZERO, false)

        texture = getTextureId(fxTexture!!)

        fbo = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.texture, 0)

        depthBuffer = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, lastSize.first, lastSize.second)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer)
    }

    override fun onNGRender(g: Graphics) {
        if(context == null){
            GL.createCapabilities()

            fxContext = GLContext.fromCurrent()
            context = GLContext.createNew(fxContext!!)
        }

        // Draw to our context
        context!!.makeCurrent()
        if(scaledWidth.toInt() != lastSize.first || scaledHeight.toInt() != lastSize.second){
            lastSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())

            updateFramebufferSize()
            fireReshapeEvent(lastSize.first, lastSize.second)
        }
        glViewport(0, 0, lastSize.first, lastSize.second)
        fireRenderEvent()
        glFinish()
        fxContext!!.makeCurrent()

        if(!fxTexture!!.isLocked)
            fxTexture!!.lock()
        g.drawTexture(fxTexture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        fxTexture!!.unlock()
    }

    private fun getTextureId(texture: RTTexture) =
        Class.forName("com.sun.prism.es2.ES2RTTexture")
            .getMethod("getNativeSourceHandle")
            .apply { isAccessible = true }
            .invoke(texture) as Int

    override fun repaint() {
        needsRepaint.set(true)
    }

}