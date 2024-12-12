package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.grapl.gl.GLProfile
import com.huskerdev.openglfx.GLExecutor
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


open class WGLDXInteropCanvas(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile,
    glDebug: Boolean,
    externalWindow: Boolean
): NGGLCanvas(canvas, executor, profile, glDebug, externalWindow) {

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
        private lateinit var d3d9Texture: D3D9.Texture
        private lateinit var interopObject: WGLDX.Object

        private lateinit var fxD3D9Texture: D3D9.Texture
        private lateinit var fxTexture: Texture
        private var fxTextureHandle = 0L

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

                d3d9Texture = device.createTexture(width, height)
                WGLDX.linkShareHandle(d3d9Texture.handle, d3d9Texture.sharedHandle)

                interopObject = interopDevice.registerObject(
                    d3d9Texture.handle,
                    interopFBO.texture,
                    GL_TEXTURE_2D,
                    WGL_ACCESS_WRITE_DISCARD_NV
                )
                return true
            }
            return false
        }

        override fun getTextureForDisplay(g: Graphics): Texture {
            val width = d3d9Texture.width
            val height = d3d9Texture.height

            if(!this::fxD3D9Texture.isInitialized || fxD3D9Texture.width != width || fxD3D9Texture.height != height){
                disposeFXResources()

                fxD3D9Texture = D3D9.Device.jfx.createTexture(width, height, d3d9Texture.sharedHandle)
                fxTexture = GLFXUtils.createPermanentFXTexture(width, height)
                fxTextureHandle = fetchDXTexHandle(fxTexture, g)
            }

            D3D9.Device.jfx.stretchRect(fxD3D9Texture.handle, fxTextureHandle)

            return fxTexture
        }

        override fun dispose() {
            if (this::interopObject.isInitialized) interopObject.release()
            if (this::fbo.isInitialized) fbo.delete()
            if (this::interopFBO.isInitialized) interopFBO.delete()
            if (this::d3d9Texture.isInitialized) d3d9Texture.release()
        }

        override fun disposeFXResources() {
            if (this::fxD3D9Texture.isInitialized) fxD3D9Texture.release()
            if (this::fxTexture.isInitialized) fxTexture.dispose()
        }
    }
}