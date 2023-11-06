package com.huskerdev.openglfx.implementations.multithread

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.GLProfile
import com.huskerdev.openglfx.GL_TEXTURE_2D
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.utils.OGLFXUtils.Companion.DX9TextureResource
import com.huskerdev.openglfx.utils.fbo.Framebuffer
import com.huskerdev.openglfx.utils.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.utils.windows.D3D9Device
import com.huskerdev.openglfx.utils.windows.D3D9Texture
import com.huskerdev.openglfx.utils.windows.DXInterop
import com.huskerdev.openglfx.utils.windows.WGL_ACCESS_WRITE_DISCARD_NV
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class MultiThreadInteropImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
) : OpenGLCanvas(profile, flipY, msaa, true){

    private val paintLock = Object()
    private val interLock = Object()

    private var lastSize = Pair(-1, -1)

    private var needsRepaint = AtomicBoolean(false)
    private var needsBlit = AtomicBoolean(false)

    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer
    private lateinit var interThreadFBO: Framebuffer

    private lateinit var context: GLContext
    private val fxDevice = D3D9Device.fxInstance

    private lateinit var fxD3DTexture: D3D9Texture
    private lateinit var fxTexture: Texture

    private var interopTexture = -1L

    private lateinit var passthroughShader: PassthroughShader
    private lateinit var fxContext: GLContext
    private lateinit var interopFBO: Framebuffer
    private var lastDXSize = Pair(-1, -1)

    init {
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsBlit.get() || needsRepaint.getAndSet(false)) {
                    NodeHelper.markDirty(this@MultiThreadInteropImpl, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@MultiThreadInteropImpl, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()

        thread(isDaemon = true) {
            context = GLContext.create(0, profile == GLProfile.Core)
            fxContext = GLContext.create(context.handle, profile == GLProfile.Core)
            context.makeCurrent()
            GLExecutor.initGLFunctions()
            executor.initGLFunctionsImpl()

            while(!disposed){
                paint()
                synchronized(interLock) {
                    fbo.blitTo(interThreadFBO.id)
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    paintLock.wait()
                }
            }
        }
    }

    private fun paint(){
        if (scaledWidth.toInt() != lastSize.first || scaledHeight.toInt() != lastSize.second) {
            lastSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
            updateFramebufferSize()

            fireReshapeEvent(lastSize.first, lastSize.second)
        }

        glViewport(0, 0, lastSize.first, lastSize.second)
        fireRenderEvent(if (msaa != 0) msaaFBO.id else fbo.id)
        if (msaa != 0)
            msaaFBO.blitTo(fbo.id)
    }

    private fun updateFramebufferSize() {
        if (::fbo.isInitialized) {
            interThreadFBO.delete()
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        val width = lastSize.first
        val height = lastSize.second

        interThreadFBO = Framebuffer(width, height)
        interThreadFBO.bindFramebuffer()

        // Create GL texture
        fbo = Framebuffer(width, height)
        fbo.bindFramebuffer()

        // Create multi-sampled framebuffer
        if(msaa != 0) {
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        }
    }

    override fun onNGRender(g: Graphics) {
        if(width == 0.0 || height == 0.0)
            return

        if (needsBlit.getAndSet(false)) {
            if(!this::passthroughShader.isInitialized){
                fxContext.makeCurrent()
                passthroughShader = PassthroughShader()
            }

            synchronized(interLock){
                if (scaledWidth.toInt() != lastDXSize.first || scaledHeight.toInt() != lastDXSize.second) {
                    lastDXSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
                    updateInteropTexture()
                }
                glViewport(0, 0, lastDXSize.first, lastDXSize.second)

                DXInterop.wglDXLockObjectsNV(DXInterop.interopHandle, interopTexture)
                passthroughShader.copy(interThreadFBO, interopFBO)
                DXInterop.wglDXUnlockObjectsNV(DXInterop.interopHandle, interopTexture)
            }
        }
        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateInteropTexture(){
        if(this::fxTexture.isInitialized) {
            DXInterop.wglDXUnregisterObjectNV(DXInterop.interopHandle, interopTexture)
            fxTexture.dispose()
            interopFBO.delete()
        }

        val width = lastDXSize.first
        val height = lastDXSize.second

        interopFBO = Framebuffer(width, height)
        interopFBO.bindFramebuffer()

        // Create and register D3D9 shared texture
        fxD3DTexture = fxDevice.createTexture(width, height)
        DXInterop.wglDXSetResourceShareHandleNV(fxD3DTexture.handle, fxD3DTexture.sharedHandle)

        // Create default JavaFX texture and replace a native handle with custom one.
        fxTexture = GraphicsPipeline.getDefaultResourceFactory().createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()
        DXInterop.replaceD3DTextureInResource(fxTexture.DX9TextureResource, fxD3DTexture.handle)

        // Create interop texture
        interopTexture = DXInterop.wglDXRegisterObjectNV(DXInterop.interopHandle, fxD3DTexture.handle, interopFBO.texture, GL_TEXTURE_2D, WGL_ACCESS_WRITE_DISCARD_NV)

        fxContext.makeCurrent()
    }

    override fun repaint() {
        synchronized(paintLock){
            paintLock.notifyAll()
        }
    }

    override fun dispose() {
        super.dispose()
        GLContext.delete(context)
    }
}