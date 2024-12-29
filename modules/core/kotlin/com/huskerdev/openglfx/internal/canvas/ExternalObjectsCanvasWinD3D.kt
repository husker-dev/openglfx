package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GL_TEXTURE_2D
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.NGGLCanvas

import com.huskerdev.openglfx.internal.Framebuffer
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.fetchDXTexHandle
import com.huskerdev.openglfx.internal.platforms.GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glCreateMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glDeleteMemoryObjectsEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glImportMemoryWin32HandleEXT
import com.huskerdev.openglfx.internal.platforms.MemoryObjects.Companion.glTextureStorageMem2DEXT
import com.huskerdev.openglfx.internal.platforms.win.D3D9
import com.sun.prism.Graphics

import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean


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
        private var memoryObj = 0

        private lateinit var texture: Texture

        private lateinit var sharedD3DTexture0: D3D9.Texture
        private lateinit var sharedD3DTexture: D3D9.Texture
        private lateinit var sharedD3DTextureSurface: D3D9.Surface

        private lateinit var fxD3DTexture: D3D9.Texture
        private lateinit var fxD3DTextureSurface: D3D9.Surface

        private val shouldUpdateBinding = AtomicBoolean()

        override fun render(width: Int, height: Int): Framebuffer {
            if(checkFramebufferSize(width, height))
                canvas.fireReshapeEvent(width, height)

            fbo.bindFramebuffer()
            canvas.fireRenderEvent(fbo.id)
            fbo.blitTo(interopFBO)

            return fbo
        }

        private fun checkFramebufferSize(width: Int, height: Int): Boolean{
            if(!this::fbo.isInitialized || fbo.width != width || fbo.height != height){
                dispose()

                sharedD3DTexture0 = d3d9Device.createTexture(width, height)

                memoryObj = glCreateMemoryObjectsEXT()
                glImportMemoryWin32HandleEXT(memoryObj, 0, GL_HANDLE_TYPE_D3D11_IMAGE_KMT_EXT, sharedD3DTexture0.sharedHandle)

                val sharedTexture = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, sharedTexture)
                glTextureStorageMem2DEXT(sharedTexture, 1, GL_BGRA, width, height, memoryObj, 0)

                interopFBO = Framebuffer.Default(width, height, texture = sharedTexture)
                fbo = createFramebufferForRender(width, height)

                shouldUpdateBinding.set(true)
                return true
            }
            return false
        }

        override fun getTextureForDisplay(g: Graphics): Texture {
            val width = sharedD3DTexture0.width
            val height = sharedD3DTexture0.height

            if(shouldUpdateBinding.getAndSet(false)){
                disposeFXResources()

                sharedD3DTexture = D3D9.Device.jfx.createTexture(width, height, sharedD3DTexture0.sharedHandle)
                sharedD3DTextureSurface = sharedD3DTexture.getSurfaceLevel(0)

                texture = GLFXUtils.createPermanentFXRTTexture(width, height)
                fxD3DTexture = D3D9.Texture(width, height, fetchDXTexHandle(texture, g), 0)
                fxD3DTextureSurface = fxD3DTexture.getSurfaceLevel(0)
            }

            D3D9.Device.jfx.stretchRect(sharedD3DTextureSurface, fxD3DTextureSurface)

            return texture
        }

        override fun dispose() {
            if (this::fbo.isInitialized) fbo.delete()
            if (this::interopFBO.isInitialized) interopFBO.delete()
            if (this::sharedD3DTexture0.isInitialized) sharedD3DTexture0.release()
            if (memoryObj != 0) glDeleteMemoryObjectsEXT(memoryObj)
        }

        override fun disposeFXResources() {
            if (this::sharedD3DTextureSurface.isInitialized) sharedD3DTextureSurface.release()
            if (this::sharedD3DTexture.isInitialized) sharedD3DTexture.release()
            if (this::fxD3DTextureSurface.isInitialized) fxD3DTextureSurface.release()
            if (this::fxD3DTexture.isInitialized) fxD3DTexture.release()
            if (this::texture.isInitialized) texture.dispose()
        }
    }
}