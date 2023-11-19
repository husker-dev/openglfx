package com.huskerdev.openglfx.canvas.implementations.async

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.canvas.GLProfile
import com.huskerdev.openglfx.canvas.OpenGLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.GLInteropType
import com.huskerdev.openglfx.internal.PassthroughShader
import com.huskerdev.openglfx.internal.Size
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import com.huskerdev.openglfx.internal.fbo.MultiSampledFramebuffer
import com.sun.javafx.scene.DirtyBits
import com.sun.javafx.scene.NodeHelper
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import javafx.animation.AnimationTimer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class AsyncSharedCanvasImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): OpenGLCanvas(GLInteropType.TextureSharing, profile, flipY, msaa, false){

    private val paintLock = Object()
    private val blitLock = Object()

    private var lastDrawSize = Size(-1, -1)
    private var lastResultSize = Size(-1, -1)

    private var parallelContext: GLContext? = null
    private var resultContext: GLContext? = null
    private var fxContext: GLContext? = null

    private lateinit var resultFBO: Framebuffer
    private lateinit var interThreadFBO: Framebuffer
    private lateinit var fbo: Framebuffer
    private lateinit var msaaFBO: MultiSampledFramebuffer

    private lateinit var fxTexture: Texture

    private var needsBlit = AtomicBoolean(false)

    private lateinit var passthroughShader: PassthroughShader

    private fun initializeGL(){
        fxContext = GLContext.current()
        parallelContext = GLContext.create(fxContext!!, profile == GLProfile.Core)
        resultContext = GLContext.create(fxContext!!, profile == GLProfile.Core)

        thread(isDaemon = true) {
            parallelContext!!.makeCurrent()
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
        lastDrawSize.onDifference(scaledWidth, scaledHeight) {
            updateDrawFramebufferSize(scaledWidth, scaledHeight)
            fireReshapeEvent(scaledWidth, scaledHeight)
        }

        glViewport(0, 0, lastDrawSize.width, lastDrawSize.height)
        fireRenderEvent(if(msaa != 0) msaaFBO.id else fbo.id)
        if(msaa != 0)
            msaaFBO.blitTo(fbo.id)
        glFinish()
    }

    override fun onNGRender(g: Graphics) {
        if (fxContext == null)
            initializeGL()

        if (needsBlit.getAndSet(false)) {
            resultContext!!.makeCurrent()

            if(!::passthroughShader.isInitialized)
                passthroughShader = PassthroughShader()

            lastResultSize.onDifference(scaledWidth, scaledHeight) {
                updateResultFramebufferSize(scaledWidth, scaledHeight)
            }
            glViewport(0, 0, lastResultSize.width, lastResultSize.height)

            synchronized(blitLock){
                passthroughShader.copy(interThreadFBO, resultFBO)
            }
            fxContext!!.makeCurrent()
        }

        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateResultFramebufferSize(width: Int, height: Int) {
        if(::resultFBO.isInitialized)
            resultFBO.delete()

        // Create JavaFX texture
        if(::fxTexture.isInitialized)
            fxTexture.dispose()
        fxTexture = GraphicsPipeline.getDefaultResourceFactory()
            .createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()

        // Create framebuffer that connected to JavaFX's texture
        resultFBO = Framebuffer(width, height, existingTexture = fxTexture.GLTextureId)
    }

    private fun updateDrawFramebufferSize(width: Int, height: Int) {
        if(::fbo.isInitialized){
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        // Create 'buffer' framebuffer
        interThreadFBO = Framebuffer(width, height)

        // Create framebuffer
        fbo = Framebuffer(width, height)
        fbo.bindFramebuffer()

        // Create multi-sampled framebuffer
        if(msaa != 0){
            msaaFBO = MultiSampledFramebuffer(msaa, width, height)
            msaaFBO.bindFramebuffer()
        }
    }

    override fun repaint() {
        synchronized(paintLock){
            paintLock.notifyAll()
        }
    }

    override fun timerTick() {
        if(needsBlit.get())
            markDirty()
    }

    override fun dispose() {
        super.dispose()
        synchronized(paintLock){
            paintLock.notifyAll()
        }
        GLContext.delete(parallelContext!!)
        GLContext.delete(resultContext!!)
    }
}