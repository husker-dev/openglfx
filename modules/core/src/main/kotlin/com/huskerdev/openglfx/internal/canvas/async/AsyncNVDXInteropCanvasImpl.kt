package com.huskerdev.openglfx.internal.canvas.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.GL_TEXTURE_2D
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.*
import com.huskerdev.openglfx.internal.GLFXUtils
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.D3DTextureResource
import com.huskerdev.openglfx.internal.PassthroughShader
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.huskerdev.openglfx.internal.d3d9.D3D9Device
import com.huskerdev.openglfx.internal.d3d9.D3D9Texture
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop
import com.huskerdev.openglfx.internal.d3d9.NVDXInterop.Companion.interopDevice
import com.huskerdev.openglfx.internal.d3d9.WGL_ACCESS_WRITE_DISCARD_NV
import com.sun.prism.Graphics
import com.sun.prism.Texture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

open class AsyncNVDXInteropCanvasImpl(
    canvas: GLCanvas,
    executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): NGGLCanvas(canvas, executor, profile, flipY, msaa){

    private val paintLock = Object()
    private val blitLock = Object()

    private var drawSize = Size()
    private var interopTextureSize = Size()
    private var resultSize = Size()

    private lateinit var resultFBO: Framebuffer
    private lateinit var interThreadFBO: Framebuffer
    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var context: GLContext
    private lateinit var resultContext: GLContext
    private val fxDevice = D3D9Device.fxInstance

    private lateinit var fxD3DTexture: D3D9Texture
    private lateinit var fxTexture: Texture

    private var needsBlit = AtomicBoolean(false)
    private lateinit var interopObject: NVDXInterop.NVDXObject

    private lateinit var passthroughShader: PassthroughShader

    private fun initializeGLThread(){
        resultContext = GLContext.create(0, profile == GLProfile.Core)
        resultContext.makeCurrent()
        GLExecutor.loadBasicFunctionPointers()
        passthroughShader = PassthroughShader()
        GLContext.clear()

        thread(isDaemon = true) {
            context = GLContext.create(resultContext.handle, profile == GLProfile.Core)
            context.makeCurrent()
            executor.initGLFunctions()

            while(!disposed){
                paint()
                synchronized(blitLock) {
                    interopTextureSize.executeOnDifferenceWith(drawSize, ::updateInterTextureSize)
                    fbo.blitTo(interThreadFBO)
                    glFinish()
                }
                needsBlit.set(true)

                synchronized(paintLock){
                    if(!disposed) paintLock.wait()
                }
            }

            // Dispose
            canvas.fireDisposeEvent()
            GLContext.clear()
            GLFXUtils.runOnRenderThread {
                if(::fxTexture.isInitialized) fxTexture.dispose()
                if(::interopObject.isInitialized) interopObject.dispose()
                GLContext.delete(context)
                GLContext.delete(resultContext)
            }
        }
    }

    private fun paint(){
        drawSize.executeOnDifferenceWith(scaledSize, ::updateFramebufferSize, canvas::fireReshapeEvent)

        glViewport(0, 0, drawSize.width, drawSize.height)
        canvas.fireRenderEvent(if (msaa != 0) msaaFBO.id else fbo.id)
        if (msaa != 0)
            msaaFBO.blitTo(fbo)
    }

    private fun updateFramebufferSize(width: Int, height: Int) {
        if (::fbo.isInitialized) {
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
    }

    private fun updateInterTextureSize(width: Int, height: Int){
        if(::interThreadFBO.isInitialized)
            interThreadFBO.delete()
        interThreadFBO = Framebuffer(width, height)
    }

    override fun renderContent(g: Graphics) {
        if(scaledWidth == 0 || scaledHeight == 0 || disposed)
            return

        if(!::resultContext.isInitialized)
            initializeGLThread()

        if (needsBlit.getAndSet(false)) {
            resultContext.makeCurrent()

            synchronized(blitLock){
                resultSize.executeOnDifferenceWith(interopTextureSize){ width, height ->
                    updateInteropTextureSize(width, height)
                    glViewport(0, 0, width, height)
                }

                interopObject.lock()
                passthroughShader.copy(interThreadFBO, resultFBO)
                interopObject.unlock()
            }
        }
        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateInteropTextureSize(width: Int, height: Int){
        if(this::fxTexture.isInitialized) {
            interopObject.dispose()
            resultFBO.delete()
            fxTexture.dispose()
        }

        resultFBO = Framebuffer(width, height)
        resultFBO.bindFramebuffer()

        // Create and register D3D9 shared texture
        fxD3DTexture = fxDevice.createTexture(width, height)
        NVDXInterop.linkShareHandle(fxD3DTexture.handle, fxD3DTexture.sharedHandle)

        // Create default JavaFX texture and replace a native handle with custom one.
        fxTexture = GLFXUtils.createPermanentFXTexture(width, height)
        D3D9Device.replaceD3DTextureInResource(fxTexture.D3DTextureResource, fxD3DTexture.handle)

        // Create interop texture
        interopObject = interopDevice.registerObject(fxD3DTexture.handle, resultFBO.texture, GL_TEXTURE_2D, WGL_ACCESS_WRITE_DISCARD_NV)
    }

    override fun repaint() {
        synchronized(paintLock){
            paintLock.notifyAll()
        }
    }

    override fun timerTick() {
        if(needsBlit.get())
            dirty()
    }

    override fun dispose() {
        super.dispose()
        repaint()
    }
}