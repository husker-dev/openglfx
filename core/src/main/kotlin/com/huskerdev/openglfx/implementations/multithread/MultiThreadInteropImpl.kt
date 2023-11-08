package com.huskerdev.openglfx.implementations.multithread

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.GLProfile
import com.huskerdev.openglfx.GL_TEXTURE_2D
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.internal.OGLFXUtils.Companion.DX9TextureResource
import com.huskerdev.openglfx.internal.PassthroughShader
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.windows.D3D9Device
import com.huskerdev.openglfx.internal.windows.D3D9Texture
import com.huskerdev.openglfx.internal.windows.DXInterop
import com.huskerdev.openglfx.internal.windows.WGL_ACCESS_WRITE_DISCARD_NV
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
    private val blitLock = Object()

    private var lastDrawSize = Pair(-1, -1)
    private var lastResultSize = Pair(-1, -1)

    private lateinit var resultFBO: Framebuffer
    private lateinit var interThreadFBO: Framebuffer
    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var context: GLContext
    private lateinit var parallelContext: GLContext
    private val fxDevice = D3D9Device.fxInstance

    private lateinit var fxD3DTexture: D3D9Texture
    private lateinit var fxTexture: Texture

    private var needsBlit = AtomicBoolean(false)
    private var interopTexture = -1L

    private lateinit var passthroughShader: PassthroughShader

    init {
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsBlit.get()) {
                    NodeHelper.markDirty(this@MultiThreadInteropImpl, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@MultiThreadInteropImpl, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()

        thread(isDaemon = true) {
            context = GLContext.create(0, profile == GLProfile.Core)
            parallelContext = GLContext.create(context.handle, profile == GLProfile.Core)
            context.makeCurrent()
            GLExecutor.initGLFunctions()
            executor.initGLFunctionsImpl()

            while(!disposed){
                paint()
                synchronized(blitLock) {
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
        if (scaledWidth.toInt() != lastDrawSize.first || scaledHeight.toInt() != lastDrawSize.second) {
            lastDrawSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
            updateFramebufferSize()

            fireReshapeEvent(lastDrawSize.first, lastDrawSize.second)
        }

        glViewport(0, 0, lastDrawSize.first, lastDrawSize.second)
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

        val width = lastDrawSize.first
        val height = lastDrawSize.second

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
                parallelContext.makeCurrent()
                passthroughShader = PassthroughShader()
            }

            synchronized(blitLock){
                if (scaledWidth.toInt() != lastResultSize.first || scaledHeight.toInt() != lastResultSize.second) {
                    lastResultSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
                    updateInteropTexture()
                }
                glViewport(0, 0, lastResultSize.first, lastResultSize.second)

                DXInterop.wglDXLockObjectsNV(DXInterop.interopHandle, interopTexture)
                passthroughShader.copy(interThreadFBO, resultFBO)
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
            resultFBO.delete()
        }

        val width = lastResultSize.first
        val height = lastResultSize.second

        resultFBO = Framebuffer(width, height)
        resultFBO.bindFramebuffer()

        // Create and register D3D9 shared texture
        fxD3DTexture = fxDevice.createTexture(width, height)
        DXInterop.wglDXSetResourceShareHandleNV(fxD3DTexture.handle, fxD3DTexture.sharedHandle)

        // Create default JavaFX texture and replace a native handle with custom one.
        fxTexture = GraphicsPipeline.getDefaultResourceFactory().createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()
        DXInterop.replaceD3DTextureInResource(fxTexture.DX9TextureResource, fxD3DTexture.handle)

        // Create interop texture
        interopTexture = DXInterop.wglDXRegisterObjectNV(DXInterop.interopHandle, fxD3DTexture.handle, resultFBO.texture, GL_TEXTURE_2D, WGL_ACCESS_WRITE_DISCARD_NV)

        parallelContext.makeCurrent()
    }

    override fun repaint() {
        synchronized(paintLock){
            paintLock.notifyAll()
        }
    }

    override fun dispose() {
        super.dispose()
        GLContext.delete(context)
        GLContext.delete(parallelContext)
    }
}