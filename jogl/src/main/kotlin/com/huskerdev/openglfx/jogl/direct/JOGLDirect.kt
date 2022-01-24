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
import jogamp.opengl.GLDrawableFactoryImpl
import java.lang.reflect.Method
import java.nio.IntBuffer


class JOGLDirect: JOGLFXCanvas() {

    private var drawableFactory: GLDrawableFactoryImpl = GLDrawableFactoryImpl.getFactoryImpl(GLProfile.getDefault())

    private lateinit var context: GLContext
    private lateinit var texture: RTTexture
    private var textureFBO = -1

    private var needTextureRedraw = true
    private val needTextureRecreation: Boolean
        get() = !this::texture.isInitialized || texture.contentWidth != scaledWidth.toInt() || texture.contentHeight != scaledHeight.toInt()

    private lateinit var getFboIDMethod: Method

    override fun onNGRender(g: Graphics) {
        if(scaledWidth == 0.0 || scaledHeight == 0.0)
            return

        if (!this::context.isInitialized)
            context = drawableFactory.createExternalGLContext()
        val gl = context.gl.gL2
        if(renderThread == null)
            renderThread = Thread.currentThread()

        if (needTextureRecreation) {
            texture = GraphicsPipeline.getDefaultResourceFactory().createRTTexture(scaledWidth.toInt(), scaledHeight.toInt(), Texture.WrapMode.CLAMP_TO_ZERO)
            texture.contentsUseful()

            if(!this::getFboIDMethod.isInitialized) {
                getFboIDMethod = texture::class.java.getDeclaredMethod("getFboID")
                getFboIDMethod.isAccessible = true
            }
            textureFBO = getFboIDMethod.invoke(texture) as Int
            needTextureRedraw = true
        } else
            texture.lock()

        if(needTextureRedraw) {
            needTextureRedraw = false

            val texGr = texture.createGraphics()
            texGr.isDepthBuffer = true
            texGr.isDepthTest = true
            texGr.clear()

            JOGLUtils.rawGL(gl) {
                if (gl !is GL2)
                    return@rawGL
                val oldBuffer = IntBuffer.allocate(1)
                gl.glGetIntegerv(GL_FRAMEBUFFER_BINDING, oldBuffer)

                gl.glBindFramebuffer(GL_FRAMEBUFFER, textureFBO)
                gl.glViewport(0, 0, scaledWidth.toInt(), scaledHeight.toInt())
                fireInitEvent(gl)
                fireReshapeEvent(gl)
                fireRenderEvent(gl)

                gl.glBindFramebuffer(GL_FRAMEBUFFER, oldBuffer[0])
            }
        }

        g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        texture.unlock()
    }

    override fun requestRepaint() {
        needTextureRedraw = true
        NodeHelper.markDirty(this, DirtyBits.NODE_GEOMETRY)
        NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS)
    }

}