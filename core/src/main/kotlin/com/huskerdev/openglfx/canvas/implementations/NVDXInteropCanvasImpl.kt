package com.huskerdev.openglfx.canvas.implementations

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.OpenGLCanvas
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.D3DTextureResource
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.d3d9.D3D9Device
import com.huskerdev.openglfx.internal.d3d9.D3D9Texture
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop.Companion.interopHandle
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop.Companion.wglDXLockObjectsNV
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop.Companion.wglDXRegisterObjectNV
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop.Companion.wglDXSetResourceShareHandleNV
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop.Companion.wglDXUnlockObjectsNV
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop.Companion.wglDXUnregisterObjectNV
import com.huskerdev.openglfx.internal.d3d9.WGL_ACCESS_WRITE_DISCARD_NV
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import java.util.concurrent.atomic.AtomicBoolean


open class NVDXInteropCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
) : OpenGLCanvas(GLInteropType.NVDXInterop, profile, flipY, msaa, false){

    private var lastSize = Size(-1, -1)
    private var initialized = false

    private var needsRepaint = AtomicBoolean(false)

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private var context: GLContext? = null
    private val fxDevice = D3D9Device.fxInstance

    private lateinit var fxD3DTexture: D3D9Texture
    private lateinit var fxTexture: Texture

    private var interopTexture = -1L

    override fun onNGRender(g: Graphics) {
        if(width == 0.0 || height == 0.0)
            return

        if(!initialized){
            initialized = true

            context = GLContext.create(0, profile == GLProfile.Core)
            context!!.makeCurrent()
            executor.initGLFunctions()
        }
        context!!.makeCurrent()

        lastSize.onDifference(scaledWidth, scaledHeight) {
            updateFramebufferSize(scaledWidth, scaledHeight)

            wglDXLockObjectsNV(interopHandle, interopTexture)
            fireReshapeEvent(scaledWidth, scaledHeight)
        }
        wglDXLockObjectsNV(interopHandle, interopTexture)

        glViewport(0, 0, lastSize.width, lastSize.height)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)
        if(msaa != 0)
            msaaFBO.blitTo(fbo.id)

        wglDXUnlockObjectsNV(interopHandle, interopTexture)
        drawResultTexture(g, fxTexture)
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
        if (::fbo.isInitialized) {
            wglDXUnregisterObjectNV(interopHandle, interopTexture)
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
        wglDXSetResourceShareHandleNV(fxD3DTexture.handle, fxD3DTexture.sharedHandle)

        // Create default JavaFX texture and replace native handle with custom one
        fxTexture = GraphicsPipeline.getDefaultResourceFactory().createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()
        NVDXInterop.replaceD3DTextureInResource(fxTexture.D3DTextureResource, fxD3DTexture.handle)

        // Create interop texture
        interopTexture = wglDXRegisterObjectNV(interopHandle, fxD3DTexture.handle, fbo.texture, GL_TEXTURE_2D, WGL_ACCESS_WRITE_DISCARD_NV)

        // For some reason the context resets by this time, so make it current again
        context!!.makeCurrent()
    }

    override fun repaint() = needsRepaint.set(true)

    override fun timerTick() {
        if(needsRepaint.getAndSet(false))
            markDirty()
    }

    override fun dispose() {
        super.dispose()
        GLContext.delete(context!!)
    }
}