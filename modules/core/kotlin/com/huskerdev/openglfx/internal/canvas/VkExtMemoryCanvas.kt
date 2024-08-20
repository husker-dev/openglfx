package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.NGGLCanvas

import com.huskerdev.openglfx.internal.Framebuffer
import com.huskerdev.openglfx.internal.platforms.GL_HANDLE_TYPE_OPAQUE_FD_EXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glCreateMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glDeleteMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glImportMemoryFdEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glTextureStorageMem2DEXT
import com.huskerdev.openglfx.internal.platforms.linux.VkExtMemory

import com.sun.prism.Texture
import com.sun.prism.es2.glTextureId


open class VkExtMemoryCanvas(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile
) : NGGLCanvas(canvas, executor, profile) {

    private val vk = VkExtMemory.createVk()

    override fun onRenderThreadInit() = Unit
    override fun createSwapBuffer() = EGLImageSwapBuffer()

    override fun dispose() {
        super.dispose()
        vk.dispose()
    }

    protected inner class EGLImageSwapBuffer: SwapBuffer() {
        private lateinit var fbo: Framebuffer
        private lateinit var interopFBO: Framebuffer.Default
        private lateinit var externalImage: VkExtMemory.ExternalImage
        private var memoryObj = 0

        private lateinit var fxTexture: Texture
        private lateinit var fxInteropFbo: Framebuffer
        private var fxMemoryObj = 0

        override fun render(width: Int, height: Int) {
            if(checkFramebufferSize(width, height))
                canvas.fireReshapeEvent(width, height)

            fbo.bindFramebuffer()
            canvas.fireRenderEvent(fbo.id)
            fbo.blitTo(interopFBO)

            glFinish()
        }

        private fun checkFramebufferSize(width: Int, height: Int): Boolean{
            if(!this::fbo.isInitialized || fbo.width != width || fbo.height != height){
                dispose()

                externalImage = vk.createExternalImage(width, height)

                memoryObj = glCreateMemoryObjectsEXT()
                glImportMemoryFdEXT(memoryObj, externalImage.size, GL_HANDLE_TYPE_OPAQUE_FD_EXT, externalImage.fd1)

                val sharedTexture = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, sharedTexture)
                glTextureStorageMem2DEXT(sharedTexture, 1, GL_RGBA8, width, height, memoryObj, 0)

                interopFBO = Framebuffer.Default(width, height, texture = sharedTexture)
                fbo = createFramebufferForRender(width, height)

                return true
            }
            return false
        }

        override fun getTextureForDisplay(): Texture {
            val width = fbo.width
            val height = fbo.height

            if(!this::fxTexture.isInitialized || fxTexture.physicalWidth != width || fxTexture.physicalHeight != height){
                disposeFXResources()

                fxTexture = GLFXUtils.createPermanentFXTexture(width, height)

                fxMemoryObj = glCreateMemoryObjectsEXT()
                glImportMemoryFdEXT(fxMemoryObj, externalImage.size, GL_HANDLE_TYPE_OPAQUE_FD_EXT, externalImage.fd2)

                val sharedTexture = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, sharedTexture)
                glTextureStorageMem2DEXT(sharedTexture, 1, GL_RGBA8, width, height, fxMemoryObj, 0)

                fxInteropFbo = Framebuffer.Default(width, height, texture = sharedTexture)
            }

            fxInteropFbo.copyToTexture(fxTexture.glTextureId)
            glFinish()

            return fxTexture
        }

        override fun dispose() {
            if (this::fbo.isInitialized) fbo.delete()
            if (this::interopFBO.isInitialized) interopFBO.delete()
            if (memoryObj != 0) glDeleteMemoryObjectsEXT(memoryObj)
            if (this::externalImage.isInitialized) externalImage.dispose()
        }

        override fun disposeFXResources() {
            if (this::fxTexture.isInitialized) fxTexture.dispose()
            if (this::fxInteropFbo.isInitialized) fxInteropFbo.delete()
            if (fxMemoryObj != 0) glDeleteMemoryObjectsEXT(fxMemoryObj)
        }
    }
}