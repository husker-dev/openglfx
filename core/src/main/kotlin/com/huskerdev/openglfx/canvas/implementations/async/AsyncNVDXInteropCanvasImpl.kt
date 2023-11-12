package com.huskerdev.openglfx.canvas.implementations.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.GL_TEXTURE_2D
import com.huskerdev.openglfx.canvas.OpenGLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.D3DTextureResource
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.PassthroughShader
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.d3d9.D3D9Device
import com.huskerdev.openglfx.internal.d3d9.D3D9Texture
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop
import com.huskerdev.openglfx.internal.d3d9.WGL_ACCESS_WRITE_DISCARD_NV
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class AsyncNVDXInteropCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
) : OpenGLCanvas(GLInteropType.NVDXInterop, profile, flipY, msaa, true){

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
                    NodeHelper.markDirty(this@AsyncNVDXInteropCanvasImpl, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@AsyncNVDXInteropCanvasImpl, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()

        thread(isDaemon = true) {
            context = GLContext.create(0, profile == GLProfile.Core)
            parallelContext = GLContext.create(context.handle, profile == GLProfile.Core)
            context.makeCurrent()
            executor.initGLFunctions()

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

                NVDXInterop.wglDXLockObjectsNV(NVDXInterop.interopHandle, interopTexture)
                passthroughShader.copy(interThreadFBO, resultFBO)
                NVDXInterop.wglDXUnlockObjectsNV(NVDXInterop.interopHandle, interopTexture)
            }
        }
        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateInteropTexture(){
        if(this::fxTexture.isInitialized) {
            NVDXInterop.wglDXUnregisterObjectNV(NVDXInterop.interopHandle, interopTexture)
            fxTexture.dispose()
            resultFBO.delete()
        }

        val width = lastResultSize.first
        val height = lastResultSize.second

        resultFBO = Framebuffer(width, height)
        resultFBO.bindFramebuffer()

        // Create and register D3D9 shared texture
        fxD3DTexture = fxDevice.createTexture(width, height)
        NVDXInterop.wglDXSetResourceShareHandleNV(fxD3DTexture.handle, fxD3DTexture.sharedHandle)

        // Create default JavaFX texture and replace a native handle with custom one.
        fxTexture = GraphicsPipeline.getDefaultResourceFactory().createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()
        NVDXInterop.replaceD3DTextureInResource(fxTexture.D3DTextureResource, fxD3DTexture.handle)

        // Create interop texture
        interopTexture = NVDXInterop.wglDXRegisterObjectNV(NVDXInterop.interopHandle, fxD3DTexture.handle, resultFBO.texture, GL_TEXTURE_2D, WGL_ACCESS_WRITE_DISCARD_NV)

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