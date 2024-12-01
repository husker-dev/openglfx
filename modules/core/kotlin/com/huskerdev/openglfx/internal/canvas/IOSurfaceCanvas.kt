package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GL_TEXTURE_RECTANGLE
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.Framebuffer
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.internal.platforms.macos.IOSurface
import com.sun.prism.Texture
import com.sun.prism.es2.glTextureId

class IOSurfaceCanvas(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile,
    glDebug: Boolean,
    externalWindow: Boolean
) : NGGLCanvas(canvas, executor, profile, glDebug, externalWindow) {

    override fun onRenderThreadInit() = Unit
    override fun createSwapBuffer() = IOSurfaceSwapBuffer()


    protected inner class IOSurfaceSwapBuffer: SwapBuffer(){
        private lateinit var fbo: Framebuffer
        private lateinit var interopFBO: Framebuffer.Default
        private lateinit var ioSurface: IOSurface

        private lateinit var fxTexture: Texture
        private lateinit var fxTextureFBO: Framebuffer
        private lateinit var fxInteropFBO: Framebuffer

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
                return true
            }
            return false
        }

        override fun getTextureForDisplay(): Texture {
            val width = ioSurface.width
            val height = ioSurface.height

            if(!::fxTexture.isInitialized || fxTexture.physicalWidth != width || fxTexture.physicalHeight != height){
                disposeFXResources()

                fxTexture = GLFXUtils.createPermanentFXTexture(width, height)
                fxTextureFBO = Framebuffer.Default(width, height,
                    texture = fxTexture.glTextureId)

                fxInteropFBO = Framebuffer.Default(width, height,
                    texture = ioSurface.createBoundTexture(),
                    textureType = GL_TEXTURE_RECTANGLE)
            }

            ioSurface.lock()
            fxInteropFBO.blitTo(fxTextureFBO)
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