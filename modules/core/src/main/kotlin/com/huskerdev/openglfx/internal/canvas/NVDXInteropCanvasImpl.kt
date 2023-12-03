package com.huskerdev.openglfx.internal.canvas

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.D3DTextureResource
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.d3d9.D3D9Device
import com.huskerdev.openglfx.internal.d3d9.D3D9Texture
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop.Companion.interopDevice
import com.huskerdev.openglfx.internal.d3d9.WGL_ACCESS_WRITE_DISCARD_NV
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean


open class NVDXInteropCanvasImpl(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile
): NGGLCanvas(canvas, executor, profile){

    private var lastSize = Size()

    private var needsRepaint = AtomicBoolean(false)

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var context: GLContext
    private val fxDevice = D3D9Device.fxInstance

    private lateinit var fxD3DTexture: D3D9Texture
    private lateinit var fxTexture: Texture

    private lateinit var interopObject: NVDXInterop.NVDXObject

    override fun renderContent(g: Graphics) {
        if(width == 0.0 || height == 0.0)
            return

        if(!::context.isInitialized){
            context = GLContext.create(0, profile == GLProfile.Core)
            context.makeCurrent()
            executor.initGLFunctions()
        }
        context.makeCurrent()

        lastSize.executeOnDifferenceWith(scaledSize) { width, height ->
            updateFramebufferSize(width, height)

            interopObject.lock()
            canvas.fireReshapeEvent(width, height)
        }
        interopObject.lock()

        glViewport(0, 0, lastSize.width, lastSize.height)
        canvas.fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)
        if(msaa != 0)
            msaaFBO.blitTo(fbo)

        interopObject.unlock()
        drawResultTexture(g, fxTexture)
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
        if (::fbo.isInitialized) {
            interopObject.dispose()
            fxTexture.dispose()

            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        // Create GL texture
        fbo = Framebuffer(width, height)
        fbo.bindFramebuffer()

        // Create multi-sampled framebuffer
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        }

        // Create and register D3D9 shared texture
        fxD3DTexture = fxDevice.createTexture(width, height)
        NVDXInterop.linkShareHandle(fxD3DTexture.handle, fxD3DTexture.sharedHandle)

        // Create default JavaFX texture and replace a native handle with custom one.
        fxTexture = GLFXUtils.createPermanentFXTexture(width, height)
        D3D9Device.replaceD3DTextureInResource(fxTexture.D3DTextureResource, fxD3DTexture.handle)

        // Create interop texture
        interopObject = interopDevice.registerObject(fxD3DTexture.handle, fbo.texture, GL_TEXTURE_2D, WGL_ACCESS_WRITE_DISCARD_NV)

        // For some reason the context resets by this time, so make it current again.
        context.makeCurrent()
    }

    override fun repaint() = needsRepaint.set(true)

    override fun timerTick() {
        if(needsRepaint.getAndSet(false))
            dirty()
    }

    override fun dispose() {
        super.dispose()
        GLFXUtils.runOnRenderThread {
            context.makeCurrent()
            canvas.fireDisposeEvent()

            if(::interopObject.isInitialized) interopObject.dispose()
            if(::fxTexture.isInitialized) fxTexture.dispose()
            if(::context.isInitialized) GLContext.delete(context)
        }
    }
}