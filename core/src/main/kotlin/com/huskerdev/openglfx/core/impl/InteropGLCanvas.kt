package com.huskerdev.openglfx.core.impl

import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.core.GLContext
import com.huskerdev.openglfx.core.GLExecutor
import com.huskerdev.openglfx.core.*
import com.huskerdev.openglfx.utils.OpenGLFXUtils.Companion.DX9TextureResource
import com.huskerdev.openglfx.core.d3d9.D3D9Device
import com.huskerdev.openglfx.core.d3d9.D3D9Texture
import com.huskerdev.openglfx.utils.WinUtils
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import java.util.concurrent.atomic.AtomicBoolean


open class InteropGLCanvas(
    private val executor: GLExecutor,
    profile: Int
) : OpenGLCanvas(profile){

    companion object {

        private const val textureStep = 400

        private var interopHandle = -1L

        init {
            WinUtils.loadLibrary()
        }
    }

    private var lastSize = Pair(-1, -1)
    private var initialized = false

    private var needsRepaint = AtomicBoolean(false)

    private var texture = -1
    private var fbo = -1
    private var depthBuffer = -1

    private var sharedTextureHandle = -1L
    private var locked = false

    private var context: GLContext? = null

    private val fxDevice = D3D9Device.fxDevice

    private lateinit var fxTexture: D3D9Texture
    private lateinit var fxTextureObject: Texture

    init {
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsRepaint.getAndSet(false)) {
                    NodeHelper.markDirty(this@InteropGLCanvas, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@InteropGLCanvas, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()
    }

    override fun onNGRender(g: Graphics) = executor.run {
        if(width == 0.0 || height == 0.0)
            return

        if(!initialized){
            initialized = true

            context = GLContext.createNew(executor, profile)
            context!!.makeCurrent()
            executor.initGLFunctions()

            if(interopHandle == -1L)
                interopHandle = wglDXOpenDeviceNV(fxDevice.handle)
        }
        context!!.makeCurrent()

        if(scaledWidth.toInt() != lastSize.first || scaledHeight.toInt() != lastSize.second) {
            lastSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
            updateFramebufferSize()

            lockInteropTexture()
            fireReshapeEvent(lastSize.first, lastSize.second)
        }

        lockInteropTexture()
        glViewport(0, 0, lastSize.first, lastSize.second)
        fireRenderEvent()
        unlockInteropTexture()

        drawResultTexture(g, fxTextureObject)
    }

    private fun lockInteropTexture(){
        if(!locked){
            locked = true
            WinUtils.wglDXLockObjectsNV(executor.getWglDXLockObjectsNVPtr(), interopHandle, sharedTextureHandle)
        }
    }

    private fun unlockInteropTexture(){
        if(locked){
            locked = false
            WinUtils.wglDXUnlockObjectsNV(executor.getWglDXUnlockObjectsNVPtr(), interopHandle, sharedTextureHandle)
        }
    }

    private fun updateFramebufferSize() = executor.run {
        val deltaWidth = lastSize.first - (if(this@InteropGLCanvas::fxTextureObject.isInitialized) fxTextureObject.contentWidth else 0)
        val deltaHeight = lastSize.second - (if(this@InteropGLCanvas::fxTextureObject.isInitialized) fxTextureObject.contentHeight else 0)

        if(deltaWidth in -textureStep..0 && deltaHeight in -textureStep..0)
            return

        if (texture != -1) {
            wglDXUnregisterObjectNV(interopHandle, sharedTextureHandle)

            fxTextureObject.dispose()

            glDeleteTextures(texture)
            glDeleteFramebuffers(fbo)
            glDeleteRenderbuffers(depthBuffer)
        }

        val width = (lastSize.first / textureStep + 1) * textureStep
        val height = (lastSize.second / textureStep + 1) * textureStep

        // Create and register DX shared texture
        fxTexture = fxDevice.createTexture(width, height)
        wglDXSetResourceShareHandleNV(fxTexture.handle, fxTexture.sharedHandle)

        // Create GL texture
        texture = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texture)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)

        fbo = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0)

        depthBuffer = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer)

        // Create interop texture
        sharedTextureHandle = wglDXRegisterObjectNV(interopHandle, fxTexture.handle, texture, GL_TEXTURE_2D, WGL_ACCESS_WRITE_DISCARD_NV)

        // Create default JavaFX texture and replace native handle to custom one
        fxTextureObject = GraphicsPipeline.getDefaultResourceFactory().createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        WinUtils.replaceD3DTextureInResource(fxTextureObject.DX9TextureResource, fxTexture.handle)
    }

    override fun repaint() = needsRepaint.set(true)
}