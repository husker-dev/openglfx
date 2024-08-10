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
import com.huskerdev.openglfx.internal.platforms.win.D3D9
import com.huskerdev.openglfx.internal.platforms.win.DXGI.Companion.GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT
import com.huskerdev.openglfx.internal.platforms.win.DXGI.Companion.glCreateMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.win.DXGI.Companion.glDeleteMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.win.DXGI.Companion.glImportMemoryWin32HandleEXT
import com.huskerdev.openglfx.internal.platforms.win.DXGI.Companion.glTextureStorageMem2DEXT

import com.sun.prism.Texture
import com.sun.prism.d3d.d3dTextureResource


open class DXGICanvas(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile
) : NGGLCanvas(canvas, executor, profile) {

    private lateinit var d3d9Device: D3D9.Device

    override fun onRenderThreadInit() {
        d3d9Device = D3D9.Device()
    }

    override fun createSwapBuffer() = DXGISwapBuffer()


    protected inner class DXGISwapBuffer: SwapBuffer() {
        private lateinit var fbo: Framebuffer
        private lateinit var interopFBO: Framebuffer.Default
        private lateinit var d3d9Texture: D3D9.Texture
        private var memoryObj = 0

        private lateinit var fxD3D9Texture: D3D9.Texture
        private lateinit var fxTexture: Texture

        override fun render(width: Int, height: Int) {
            if(checkFramebufferSize(width, height, d3d9Device))
                canvas.fireReshapeEvent(width, height)

            fbo.bindFramebuffer()
            canvas.fireRenderEvent(fbo.id)
            fbo.blitTo(interopFBO)

            glFinish()
        }

        private fun checkFramebufferSize(width: Int, height: Int, device9: D3D9.Device): Boolean{
            if(!this::fbo.isInitialized || fbo.width != width || fbo.height != height){
                dispose()

                val sharedTexture = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, sharedTexture)

                d3d9Texture = device9.createTexture(width, height)

                memoryObj = glCreateMemoryObjectsEXT()
                glImportMemoryWin32HandleEXT(memoryObj, (width * height * 4 * 2).toLong(), GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT, d3d9Texture.sharedHandle)
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