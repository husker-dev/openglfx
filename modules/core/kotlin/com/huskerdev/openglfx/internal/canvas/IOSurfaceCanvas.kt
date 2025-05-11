package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.openglfx.GLExecutor.Companion.glDisable
import com.huskerdev.openglfx.GLExecutor.Companion.glEnable
import com.huskerdev.openglfx.GLExecutor.Companion.glGetInteger
import com.huskerdev.openglfx.GL_SCISSOR_TEST
import com.huskerdev.openglfx.GL_TEXTURE_RECTANGLE
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.Framebuffer
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.fetchGLTexId
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.internal.platforms.macos.IOSurface
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean

open class IOSurfaceCanvas(
    canvas: GLCanvas
) : NGGLCanvas(canvas) {

    override fun onRenderThreadInit() = Unit
    override fun onRenderThreadEnd() = Unit
    override fun createSwapBuffer() = IOSurfaceSwapBuffer()


    protected inner class IOSurfaceSwapBuffer: SwapBuffer(){
        private lateinit var fbo: Framebuffer
        private lateinit var interopFBO: Framebuffer.Default
        private lateinit var ioSurface: IOSurface

        private lateinit var fxTexture: Texture
        private lateinit var fxTextureFBO: Framebuffer
        private lateinit var fxInteropFBO: Framebuffer

        private val shouldUpdateBinding = AtomicBoolean()

        override fun render(width: Int, height: Int): Framebuffer {
            if(checkFramebufferSize(width, height))
                canvas.fireReshapeEvent(width, height)

            fbo.bindFramebuffer()
            canvas.fireRenderEvent(fbo.id)

            ioSurface.lock()
            fbo.blitTo(interopFBO)
            ioSurface.unlock()

            return fbo
        }

        private fun checkFramebufferSize(width: Int, height: Int): Boolean {
            if(!::ioSurface.isInitialized || ioSurface.width != width || ioSurface.height != height){
                dispose()

                ioSurface = IOSurface(width, height)
                interopFBO = Framebuffer.Default(width, height,
                    texture = ioSurface.createBoundTexture(),
                    textureType = GL_TEXTURE_RECTANGLE)

                fbo = createFramebufferForRender(width, height)

                shouldUpdateBinding.set(true)
                return true
            }
            return false
        }

        override fun getTextureForDisplay(g: Graphics): Texture {
            val width = ioSurface.width
            val height = ioSurface.height

            if(shouldUpdateBinding.getAndSet(false)){
                disposeFXResources()

                fxTexture = GLFXUtils.createPermanentFXTexture(width, height)
                fxTextureFBO = Framebuffer.Default(width, height,
                    texture = fetchGLTexId(fxTexture, g))

                fxInteropFBO = Framebuffer.Default(width, height,
                    texture = ioSurface.createBoundTexture(),
                    textureType = GL_TEXTURE_RECTANGLE)
            }

            ioSurface.lock()

            val scissorTestEnabled = glGetInteger(GL_SCISSOR_TEST) != 0
            glDisable(GL_SCISSOR_TEST)
            fxInteropFBO.blitTo(fxTextureFBO)
            if (scissorTestEnabled) {
                glEnable(GL_SCISSOR_TEST)
            }
            ioSurface.unlock()

            return fxTexture
        }

        override fun dispose() {
            if(::ioSurface.isInitialized) ioSurface.dispose()
            if(::interopFBO.isInitialized) interopFBO.delete()
            if(::fbo.isInitialized) fbo.delete()
        }

        override fun disposeFXResources() {
            if(::fxTexture.isInitialized) fxTexture.dispose()
            if(::fxTextureFBO.isInitialized) fxTextureFBO.delete()
            if(::fxInteropFBO.isInitialized) fxInteropFBO.delete()
        }
    }
}