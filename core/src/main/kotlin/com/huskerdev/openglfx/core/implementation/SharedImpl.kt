package com.huskerdev.openglfx.core.implementation

import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.core.*
import com.huskerdev.openglfx.utils.OpenGLFXUtils.Companion.GLTextureId
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.*
import javafx.animation.AnimationTimer
import java.util.concurrent.atomic.AtomicBoolean

open class SharedImpl(
    private val executor: GLExecutor,
    profile: Int
): OpenGLCanvas(profile){

    private var lastSize = Pair(-1, -1)

    private var context: GLContext? = null
    private var fxContext: GLContext? = null

    private var texture = -1
    private var fbo = -1
    private var depthBuffer = -1

    private var fxTexture: Texture? = null

    private var needsRepaint = AtomicBoolean(false)

    init {
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsRepaint.getAndSet(false)) {
                    NodeHelper.markDirty(this@SharedImpl, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@SharedImpl, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()
    }

    override fun onNGRender(g: Graphics) {
        executor.apply {
            if (context == null) {
                executor.initGLFunctions()

                fxContext = GLContext.fromCurrent(executor)
                context = GLContext.createNew(executor, profile, fxContext!!)
            }

            context!!.makeCurrent()
            if (scaledWidth.toInt() != lastSize.first || scaledHeight.toInt() != lastSize.second) {
                lastSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())

                updateFramebufferSize()
                fireReshapeEvent(lastSize.first, lastSize.second)
            }

            glViewport(0, 0, lastSize.first, lastSize.second)
            fireRenderEvent()
            glFinish()
            fxContext!!.makeCurrent()

            drawResultTexture(g, fxTexture!!)
        }
    }

    private fun updateFramebufferSize() = executor.apply {
        if(fbo != -1)
            glDeleteFramebuffers(fbo)
        if(depthBuffer != -1)
            glDeleteRenderbuffers(depthBuffer)

        fxTexture?.dispose()
        fxTexture = GraphicsPipeline.getDefaultResourceFactory().createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, lastSize.first, lastSize.second)
        fxTexture!!.makePermanent()

        texture = fxTexture!!.GLTextureId

        fbo = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0)

        depthBuffer = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, lastSize.first, lastSize.second)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer)
    }

    override fun repaint() = needsRepaint.set(true)
}