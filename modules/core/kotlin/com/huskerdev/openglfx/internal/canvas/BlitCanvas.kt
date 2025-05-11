package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.NGGLCanvas

import com.huskerdev.openglfx.internal.Framebuffer
import com.sun.prism.Graphics
import com.sun.prism.PixelFormat


import com.sun.prism.Texture
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


open class BlitCanvas(
    canvas: GLCanvas
) : NGGLCanvas(canvas) {

    override fun onRenderThreadInit() = Unit
    override fun onRenderThreadEnd() = Unit
    override fun createSwapBuffer() = BlitSwapBuffer()

    protected inner class BlitSwapBuffer: SwapBuffer() {
        private lateinit var fbo: Framebuffer
        private lateinit var interopFBO: Framebuffer.Default
        private lateinit var dataBuffer: ByteBuffer

        private lateinit var fxTexture: Texture
        private var contentChanged = false

        private val shouldUpdateBinding = AtomicBoolean()

        override fun render(width: Int, height: Int): Framebuffer {
            if(checkFramebufferSize(width, height))
                canvas.fireReshapeEvent(width, height)

            fbo.bindFramebuffer()
            canvas.fireRenderEvent(fbo.id)
            fbo.blitTo(interopFBO)

            interopFBO.readPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, dataBuffer)
            contentChanged = true

            return fbo
        }

        private fun checkFramebufferSize(width: Int, height: Int): Boolean{
            if(!this::fbo.isInitialized || fbo.width != width || fbo.height != height){
                dispose()

                dataBuffer = GLFXUtils.createDirectBuffer(width * height * 4)
                interopFBO = Framebuffer.Default(width, height)
                fbo = createFramebufferForRender(width, height)

                shouldUpdateBinding.set(true)
                return true
            }
            return false
        }

        override fun getTextureForDisplay(g: Graphics): Texture {
            val width = fbo.width
            val height = fbo.height

            if(shouldUpdateBinding.getAndSet(false)){
                disposeFXResources()

                fxTexture = GLFXUtils.createPermanentFXTexture(width, height)
            }

            if(contentChanged) {
                contentChanged = false
                fxTexture.update(
                    dataBuffer, PixelFormat.BYTE_BGRA_PRE,
                    0, 0,
                    0, 0,
                    width, height,
                    width * 4, true
                )
            }

            return fxTexture
        }

        override fun dispose() {
            if (this::fbo.isInitialized) fbo.delete()
            if (this::interopFBO.isInitialized) interopFBO.delete()
            if (this::dataBuffer.isInitialized) GLFXUtils.cleanDirectBuffer(dataBuffer)
        }

        override fun disposeFXResources() {
            if (this::fxTexture.isInitialized) fxTexture.dispose()
        }
    }
}