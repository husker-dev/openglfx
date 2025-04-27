package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.openglfx.GL_TEXTURE_2D
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.internal.Framebuffer
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.fetchDXTexHandle
import com.huskerdev.openglfx.internal.platforms.win.*
import com.huskerdev.openglfx.internal.platforms.win.WGLDX.Companion.WGL_ACCESS_WRITE_DISCARD_NV
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean


open class WGLDXInteropCanvas(
    canvas: GLCanvas
): NGGLCanvas(canvas) {

    private lateinit var d3d9Device: D3D9.Device
    private lateinit var interopDevice: WGLDX.Device

    override fun onRenderThreadInit() {
        d3d9Device = D3D9.Device()
        interopDevice = WGLDX.Device(d3d9Device)
    }

    override fun createSwapBuffer() = NVDXInteropSwapBuffer()


    protected inner class NVDXInteropSwapBuffer: SwapBuffer(){
        private lateinit var fbo: Framebuffer
        private lateinit var interopFBO: Framebuffer.Default
        private lateinit var interopObject: WGLDX.Object

        private lateinit var texture: Texture

        private lateinit var sharedD3DTexture0: D3D9.Texture
        private lateinit var sharedD3DTexture: D3D9.Texture
        private lateinit var sharedD3DTextureSurface: D3D9.Surface

        private lateinit var fxD3DTexture: D3D9.Texture
        private lateinit var fxD3DTextureSurface: D3D9.Surface

        private val shouldUpdateBinding = AtomicBoolean()

        override fun render(width: Int, height: Int): Framebuffer {
            if(checkFramebufferSize(width, height, d3d9Device, interopDevice))
                canvas.fireReshapeEvent(width, height)

            fbo.bindFramebuffer()
            canvas.fireRenderEvent(fbo.id)

            interopObject.lock()
            fbo.blitTo(interopFBO)
            interopObject.unlock()
            return fbo
        }

        private fun checkFramebufferSize(width: Int, height: Int, device: D3D9.Device, interopDevice: WGLDX.Device): Boolean{
            if(!this::fbo.isInitialized || fbo.width != width || fbo.height != height){
                dispose()

                fbo = createFramebufferForRender(width, height)

                interopFBO = Framebuffer.Default(width, height)
                interopFBO.bindFramebuffer()

                sharedD3DTexture0 = device.createTexture(width, height)
                WGLDX.linkShareHandle(sharedD3DTexture0.handle, sharedD3DTexture0.sharedHandle)

                interopObject = interopDevice.registerObject(
                    sharedD3DTexture0.handle,
                    interopFBO.texture,
                    GL_TEXTURE_2D,
                    WGL_ACCESS_WRITE_DISCARD_NV
                )

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

                texture = GLFXUtils.createPermanentFXTexture(width, height)
                fxD3DTexture = D3D9.Texture(width, height, fetchDXTexHandle(texture, g), 0)
                fxD3DTextureSurface = fxD3DTexture.getSurfaceLevel(0)
            }

            D3D9.Device.jfx.stretchRect(sharedD3DTextureSurface, fxD3DTextureSurface)

            return texture
        }

        override fun dispose() {
            if (this::interopObject.isInitialized) interopObject.release()
            if (this::fbo.isInitialized) fbo.delete()
            if (this::interopFBO.isInitialized) interopFBO.delete()
            if (this::sharedD3DTexture0.isInitialized) sharedD3DTexture0.release()
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