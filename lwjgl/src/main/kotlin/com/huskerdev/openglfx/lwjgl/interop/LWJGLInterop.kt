package com.huskerdev.openglfx.lwjgl.interop

import com.huskerdev.openglfx.lwjgl.LWJGLCanvas
import com.huskerdev.openglfx.lwjgl.utils.GLContext
import com.huskerdev.openglfx.utils.OpenGLFXUtils
import com.huskerdev.openglfx.utils.d3d9.D3D9Device
import com.huskerdev.openglfx.utils.d3d9.D3D9Texture
import com.huskerdev.openglfx.utils.d3d9.D3D9Utils
import com.huskerdev.openglfx.utils.d3d9.D3D9Utils.Companion.textureResource
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import org.lwjgl.PointerBuffer
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.WGLNVDXInterop.*
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class LWJGLInterop: LWJGLCanvas() {

    companion object {

        private const val textureStep = 400

        init {
            D3D9Utils.loadLibrary()
        }
    }

    private var lastSize = Pair(-1, -1)
    private var initialized = false

    private var needsRepaint = AtomicBoolean(false)

    private var texture = -1
    private var fbo = -1
    private var depthBuffer = -1

    private var interopHandle = -1L
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
                try {
                    if(needsRepaint.getAndSet(false)) {
                        NodeHelper.markDirty(this@LWJGLInterop, DirtyBits.NODE_BOUNDS)
                        NodeHelper.markDirty(this@LWJGLInterop, DirtyBits.REGION_SHAPE)
                    }
                } catch (_: Exception){}
            }
        }.start()
    }

    override fun onNGRender(g: Graphics) {
        if(width == 0.0 || height == 0.0)
            return

        if(!initialized){
            initialized = true

            context = GLContext.createNew()
            context!!.makeCurrent()
            GL.createCapabilities()

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

        if(!fxTextureObject.isLocked)
            fxTextureObject.lock()
        g.drawTexture(fxTextureObject, 0f, 0f, width.toFloat() + 0.5f, height.toFloat() + 0.5f, 0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
        fxTextureObject.unlock()
    }

    private fun lockInteropTexture(){
        if(!locked){
            locked = true
            D3D9Utils.wglDXLockObjectsNV(GL.getCapabilitiesWGL().wglDXLockObjectsNV, interopHandle, sharedTextureHandle)
        }
    }

    private fun unlockInteropTexture(){
        if(locked){
            locked = false
            D3D9Utils.wglDXUnlockObjectsNV(GL.getCapabilitiesWGL().wglDXUnlockObjectsNV, interopHandle, sharedTextureHandle)
        }
    }

    private fun updateFramebufferSize() {
        val deltaWidth = lastSize.first - (if(this::fxTextureObject.isInitialized) fxTextureObject.contentWidth else 0)
        val deltaHeight = lastSize.second - (if(this::fxTextureObject.isInitialized) fxTextureObject.contentHeight else 0)

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
        D3D9Utils.replaceTextureInResource(fxTextureObject.textureResource, fxTexture.handle)
    }

    override fun repaint() = needsRepaint.set(true)

}