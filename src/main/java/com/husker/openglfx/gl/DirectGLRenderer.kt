package com.husker.openglfx.gl

import com.husker.openglfx.OpenGLCanvas
import com.husker.openglfx.utils.FXUtils
import com.jogamp.opengl.*
import com.jogamp.opengl.GL.*
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.RTTexture
import com.sun.prism.Texture
import jogamp.opengl.GLDrawableFactoryImpl
import java.nio.IntBuffer


class DirectGLRenderer(capabilities: GLCapabilities): OpenGLCanvas(capabilities) {

    private var drawableFactory: GLDrawableFactoryImpl = GLDrawableFactoryImpl.getFactoryImpl(capabilities.glProfile)

    private lateinit var context: GLContext
    private lateinit var texture: RTTexture
    private var textureFBO = -1

    override fun onRender(g: Graphics) {
        if(scaledWidth == 0.0 || scaledHeight == 0.0)
            return

        if (!this::context.isInitialized)
            context = drawableFactory.createExternalGLContext()
        val gl = context.gl.gL2
        context.makeCurrent()

        if(!this::texture.isInitialized || texture.contentWidth != scaledWidth.toInt() || texture.contentHeight != scaledHeight.toInt()){
            texture = GraphicsPipeline.getDefaultResourceFactory().createRTTexture(scaledWidth.toInt(), scaledHeight.toInt(), Texture.WrapMode.CLAMP_TO_ZERO)
            texture.contentsUseful()

            val getFboIDMethod = texture::class.java.getDeclaredMethod("getFboID")
            getFboIDMethod.isAccessible = true
            textureFBO = getFboIDMethod.invoke(texture) as Int
        }else
            texture.lock()

        val texGr = texture.createGraphics()
        texGr.isDepthBuffer = true
        texGr.isDepthTest = true
        texGr.clear()

        FXUtils.rawGL(gl, this){
            if(gl !is GL2)
                return@rawGL
            val oldBuffer = IntBuffer.allocate(1)
            gl.glGetIntegerv(GL_FRAMEBUFFER_BINDING, oldBuffer)

            gl.glBindFramebuffer(GL_FRAMEBUFFER, textureFBO)
            gl.glViewport(0, 0, scaledWidth.toInt(), scaledHeight.toInt())
            fireInitEvent(gl)
            fireReshapeEvent(gl)
            fireDisplayEvent(gl)

            gl.glBindFramebuffer(GL_FRAMEBUFFER, oldBuffer[0])
        }

        g.drawTexture(texture, 0f, 0f, width.toFloat(), height.toFloat(), 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        texture.unlock()
    }

}