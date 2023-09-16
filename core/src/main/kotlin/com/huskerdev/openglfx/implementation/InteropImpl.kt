package com.huskerdev.openglfx.implementation

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.GLExecutor.Companion.initGLFunctions
import com.huskerdev.openglfx.utils.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.utils.OGLFXUtils.Companion.DX9TextureResource
import com.huskerdev.openglfx.utils.fbo.Framebuffer
import com.huskerdev.openglfx.utils.windows.D3D9Device
import com.huskerdev.openglfx.utils.windows.D3D9Texture
import com.huskerdev.openglfx.utils.windows.DXInterop
import com.huskerdev.openglfx.utils.windows.DXInterop.Companion.interopHandle
import com.huskerdev.openglfx.utils.windows.DXInterop.Companion.wglDXLockObjectsNV
import com.huskerdev.openglfx.utils.windows.DXInterop.Companion.wglDXOpenDeviceNV
import com.huskerdev.openglfx.utils.windows.DXInterop.Companion.wglDXRegisterObjectNV
import com.huskerdev.openglfx.utils.windows.DXInterop.Companion.wglDXSetResourceShareHandleNV
import com.huskerdev.openglfx.utils.windows.DXInterop.Companion.wglDXUnlockObjectsNV
import com.huskerdev.openglfx.utils.windows.DXInterop.Companion.wglDXUnregisterObjectNV
import com.huskerdev.openglfx.utils.windows.WGL_ACCESS_WRITE_DISCARD_NV
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import java.util.concurrent.atomic.AtomicBoolean


open class InteropImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
) : OpenGLCanvas(profile, flipY, msaa){

    private var lastSize = Pair(-1, -1)
    private var initialized = false

    private var needsRepaint = AtomicBoolean(false)

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private var context: GLContext? = null
    private val fxDevice = D3D9Device.fxInstance

    private lateinit var fxD3DTexture: D3D9Texture
    private lateinit var fxTexture: Texture

    private var interopTexture = -1L

    init {
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsRepaint.getAndSet(false)) {
                    NodeHelper.markDirty(this@InteropImpl, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@InteropImpl, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()
    }

    override fun onNGRender(g: Graphics) {
        if(width == 0.0 || height == 0.0)
            return

        if(!initialized){
            initialized = true

            context = GLContext.create(0, profile == GLProfile.Core)
            context!!.makeCurrent()
            initGLFunctions()
            executor.initGLFunctionsImpl()

            if (interopHandle == 0L)
                interopHandle = wglDXOpenDeviceNV(fxDevice.handle)
        }

        context!!.makeCurrent()
        if(scaledWidth.toInt() != lastSize.first || scaledHeight.toInt() != lastSize.second) {
            lastSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
            updateFramebufferSize()

            wglDXLockObjectsNV(interopHandle, interopTexture)
            fireReshapeEvent(lastSize.first, lastSize.second)
        }else
            wglDXLockObjectsNV(interopHandle, interopTexture)

        glViewport(0, 0, lastSize.first, lastSize.second)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)
        if(msaa != 0)
            msaaFBO.blitTo(fbo.id)

        wglDXUnlockObjectsNV(interopHandle, interopTexture)
        drawResultTexture(g, fxTexture)
    }

    private fun updateFramebufferSize() {
        if (::fbo.isInitialized) {
            wglDXUnregisterObjectNV(interopHandle, interopTexture)
            fxTexture.dispose()

            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        val width = lastSize.first
        val height = lastSize.second

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
        DXInterop.replaceD3DTextureInResource(fxTexture.DX9TextureResource, fxD3DTexture.handle)

        // Create interop texture
        interopTexture = wglDXRegisterObjectNV(interopHandle, fxD3DTexture.handle, fbo.texture, GL_TEXTURE_2D, WGL_ACCESS_WRITE_DISCARD_NV)

        // For some reason the context resets by this time, so make it current again
        context!!.makeCurrent()
    }

    override fun repaint() = needsRepaint.set(true)
}