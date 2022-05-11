package com.huskerdev.openglfx.jogl.direct

import com.huskerdev.openglfx.jogl.JOGLFXCanvas
import com.huskerdev.openglfx.jogl.utils.JOGLUtils
import com.jogamp.opengl.*
import com.jogamp.opengl.GL.*
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.RTTexture
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import jogamp.opengl.GLDrawableFactoryImpl
import java.lang.reflect.Method
import java.nio.IntBuffer
import java.util.concurrent.atomic.AtomicBoolean


class JOGLDirect : JOGLFXCanvas() {

    override lateinit var gl: GL2

    private val factory = GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault())

    private lateinit var context: GLContext
    private lateinit var texture: RTTexture
    private var textureFBO = -1

    private var needsRepaint = AtomicBoolean(false)
    private var needTextureRepaint = true

    private lateinit var getFboIDMethod: Method

    init {
        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsRepaint.getAndSet(false)) {
                    needTextureRepaint = true
                    NodeHelper.markDirty(this@JOGLDirect, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@JOGLDirect, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()
    }

    override fun onNGRender(g: Graphics) {
        if(scaledWidth == 0.0 || scaledHeight == 0.0)
            return

        if (!this::context.isInitialized)
            context = factory.createExternalGLContext()
        gl = context.gl.gL2

        if (!this::texture.isInitialized || texture.contentWidth != scaledWidth.toInt() || texture.contentHeight != scaledHeight.toInt()) {
            texture = GraphicsPipeline.getDefaultResourceFactory().createRTTexture(scaledWidth.toInt(), scaledHeight.toInt(), Texture.WrapMode.CLAMP_TO_ZERO, false)
            texture.contentsUseful()

            if(!this::getFboIDMethod.isInitialized) {
                getFboIDMethod = texture::class.java.getDeclaredMethod("getFboID")
                getFboIDMethod.isAccessible = true
            }
            textureFBO = getFboIDMethod.invoke(texture) as Int
            needTextureRepaint = true
        } else
            texture.lock()

        if(needTextureRepaint) {
            needTextureRepaint = false

            val texGr = texture.createGraphics()
            texGr.isDepthBuffer = true
            texGr.isDepthTest = true
            texGr.clear()

            JOGLUtils.rawGL(gl) {
                gl.glBindFramebuffer(GL_FRAMEBUFFER, textureFBO)
                gl.glViewport(0, 0, scaledWidth.toInt(), scaledHeight.toInt())
                fireInitEvent()
                fireReshapeEvent(scaledWidth.toInt(), scaledHeight.toInt())
                fireRenderEvent()
            }
        }

        g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        texture.unlock()
    }

    override fun repaint() = needsRepaint.set(true)

}