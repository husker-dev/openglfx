package com.huskerdev.openglfx.lwjgl.direct

import com.huskerdev.openglfx.lwjgl.LWJGLCanvas
import com.huskerdev.openglfx.lwjgl.utils.LWJGLUtils
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.RTTexture
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack.stackPush
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean


class LWJGLDirect: LWJGLCanvas() {

    private lateinit var texture: RTTexture
    private var textureFBO = -1

    private var needsRepaint = AtomicBoolean(false)

    private var needTextureRedraw = true
    private val needTextureRecreation: Boolean
        get() = !this::texture.isInitialized || texture.contentWidth != scaledWidth.toInt() || texture.contentHeight != scaledHeight.toInt()

    private lateinit var getFboIDMethod: Method

    private var needGLInit = true

    init {
        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsRepaint.get()) {
                    needsRepaint.set(false)

                    needTextureRedraw = true
                    NodeHelper.markDirty(this@LWJGLDirect, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@LWJGLDirect, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()
    }

    override fun onNGRender(g: Graphics) {
        if(scaledWidth == 0.0 || scaledHeight == 0.0)
            return

        if(needGLInit){
            needGLInit = false
            GL.createCapabilities()
        }

        if (needTextureRecreation) {
            texture = GraphicsPipeline.getDefaultResourceFactory().createRTTexture(scaledWidth.toInt(), scaledHeight.toInt(), Texture.WrapMode.CLAMP_TO_ZERO, false)
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

            LWJGLUtils.rawGL {
                val stack = stackPush()

                val oldBuffer = stack.mallocInt(1)
                glGetIntegerv(GL_FRAMEBUFFER_BINDING, oldBuffer)

                glBindFramebuffer(GL_FRAMEBUFFER, textureFBO)
                glViewport(0, 0, scaledWidth.toInt(), scaledHeight.toInt())
                fireInitEvent()
                fireReshapeEvent(scaledWidth.toInt(), scaledHeight.toInt())
                fireRenderEvent()

                glBindFramebuffer(GL_FRAMEBUFFER, oldBuffer[0])

                stack.close()
            }
        }

        g.drawTexture(texture, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        texture.unlock()
    }

    override fun repaint() = needsRepaint.set(true)
}