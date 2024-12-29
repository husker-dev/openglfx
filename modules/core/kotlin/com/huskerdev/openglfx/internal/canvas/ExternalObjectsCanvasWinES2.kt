package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GL_TEXTURE_2D
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.NGGLCanvas

import com.huskerdev.openglfx.internal.Framebuffer
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.fetchGLTexId
import com.huskerdev.openglfx.internal.platforms.GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glCreateMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glDeleteMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glImportMemoryWin32HandleEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glTextureStorageMem2DEXT
import com.huskerdev.openglfx.internal.platforms.VkExtMemory
import com.sun.prism.Graphics

import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean


open class ExternalObjectsCanvasWinES2(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile,
    glDebug: Boolean,
    externalWindow: Boolean
): NGGLCanvas(canvas, executor, profile, glDebug, externalWindow) {

    private val vk = VkExtMemory.createVk()

    override fun onRenderThreadInit() = Unit
    override fun createSwapBuffer() = ExternalObjectsSwapBuffer()

    override fun dispose() {
        super.dispose()
        vk.dispose()
    }

    protected inner class ExternalObjectsSwapBuffer: SwapBuffer() {
        private lateinit var fbo: Framebuffer
        private lateinit var interopFBO: Framebuffer.Default
        private lateinit var externalImage: VkExtMemory.ExternalImage
        private var memoryObj = 0

        private lateinit var fxTexture: Texture
        private var fxTextureId = 0
        private lateinit var fxInteropFbo: Framebuffer
        private var fxMemoryObj = 0

        private val shouldUpdateBinding = AtomicBoolean()

        override fun render(width: Int, height: Int): Framebuffer {
            if(checkFramebufferSize(width, height))
                canvas.fireReshapeEvent(width, height)

            fbo.bindFramebuffer()
            canvas.fireRenderEvent(fbo.id)
            fbo.blitTo(interopFBO)

            glFinish()
            return fbo
        }

        private fun checkFramebufferSize(width: Int, height: Int): Boolean{
            if(!this::fbo.isInitialized || fbo.width != width || fbo.height != height){
                dispose()

                externalImage = vk.createExternalImage(width, height)

                memoryObj = glCreateMemoryObjectsEXT()
                glImportMemoryWin32HandleEXT(memoryObj, externalImage.size, GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT, externalImage.getMemoryWin32Handle())

                val sharedTexture = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, sharedTexture)
                glTextureStorageMem2DEXT(sharedTexture, 1, GL_RGBA8, width, height, memoryObj, 0)

                interopFBO = Framebuffer.Default(width, height, texture = sharedTexture)
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
                fxTextureId = fetchGLTexId(fxTexture, g)

                fxMemoryObj = glCreateMemoryObjectsEXT()
                glImportMemoryWin32HandleEXT(fxMemoryObj, externalImage.size, GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT, externalImage.getMemoryWin32Handle())

                val sharedTexture = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, sharedTexture)
                glTextureStorageMem2DEXT(sharedTexture, 1, GL_RGBA8, width, height, fxMemoryObj, 0)

                fxInteropFbo = Framebuffer.Default(width, height, texture = sharedTexture)
            }

            fxInteropFbo.copyToTexture(fxTextureId)

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
        }
    }
}