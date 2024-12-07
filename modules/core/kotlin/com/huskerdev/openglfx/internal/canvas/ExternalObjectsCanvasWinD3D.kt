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
import com.huskerdev.openglfx.internal.platforms.GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glCreateMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glDeleteMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glImportMemoryWin32HandleEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glTextureStorageMem2DEXT
import com.huskerdev.openglfx.internal.platforms.win.D3D9

import com.sun.prism.Texture
import com.sun.prism.d3d.d3dTextureResource


open class ExternalObjectsCanvasWinD3D(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile,
    glDebug: Boolean,
    externalWindow: Boolean
) : NGGLCanvas(canvas, executor, profile, glDebug, externalWindow) {

    private lateinit var d3d9Device: D3D9.Device

    override fun onRenderThreadInit() {
        d3d9Device = D3D9.Device()
    }

    override fun createSwapBuffer() = ExternalObjectsSwapBuffer()


    protected inner class ExternalObjectsSwapBuffer: SwapBuffer() {
        private lateinit var fbo: Framebuffer
        private lateinit var interopFBO: Framebuffer.Default
        private lateinit var d3d9Texture: D3D9.Texture
        private var memoryObj = 0

        private lateinit var fxD3D9Texture: D3D9.Texture
        private lateinit var fxTexture: Texture

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

                d3d9Texture = d3d9Device.createTexture(width, height)

                memoryObj = glCreateMemoryObjectsEXT()
                glImportMemoryWin32HandleEXT(memoryObj, 0, GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT, d3d9Texture.sharedHandle)

                val sharedTexture = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, sharedTexture)
                glTextureStorageMem2DEXT(sharedTexture, 1, GL_BGRA, width, height, memoryObj, 0)

                interopFBO = Framebuffer.Default(width, height, texture = sharedTexture)
                fbo = createFramebufferForRender(width, height)

                return true
            }
            return false
        }

        override fun getTextureForDisplay(): Texture {
            val width = d3d9Texture.width
            val height = d3d9Texture.height

            if(!this::fxD3D9Texture.isInitialized || fxD3D9Texture.width != width || fxD3D9Texture.height != height){
                disposeFXResources()

                fxD3D9Texture = D3D9.Device.jfx.createTexture(width, height, d3d9Texture.sharedHandle)
                fxTexture = GLFXUtils.createPermanentFXTexture(width, height)
                D3D9.replaceD3DTextureInResource(fxTexture.d3dTextureResource, fxD3D9Texture.handle)
            }

            return fxTexture
        }

        override fun dispose() {
            if (this::fbo.isInitialized) fbo.delete()
            if (this::interopFBO.isInitialized) interopFBO.delete()
            if (this::d3d9Texture.isInitialized) d3d9Texture.release()
            if (memoryObj != 0) glDeleteMemoryObjectsEXT(memoryObj)
        }

        override fun disposeFXResources() {
            if (this::fxTexture.isInitialized) fxTexture.dispose()
        }
    }
}