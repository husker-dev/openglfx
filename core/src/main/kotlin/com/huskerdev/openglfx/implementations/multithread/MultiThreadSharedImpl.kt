package com.huskerdev.openglfx.implementations.multithread

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.GLExecutor
import com.huskerdev.openglfx.GLExecutor.Companion.glFinish
import com.huskerdev.openglfx.GLExecutor.Companion.glViewport
import com.huskerdev.openglfx.GLProfile
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.internal.OGLFXUtils.Companion.GLTextureId
import com.huskerdev.openglfx.internal.PassthroughShader
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

class MultiThreadSharedImpl(
    private val executor: GLExecutor,
    profile: GLProfile,
    flipY: Boolean,
    msaa: Int
): OpenGLCanvas(profile, flipY, msaa, false){

    private val paintLock = Object()
    private val blitLock = Object()

    private var lastDrawSize = Pair(-1, -1)
    private var lastResultSize = Pair(-1, -1)

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

    init {
        visibleProperty().addListener { _, _, _ -> repaint() }
        widthProperty().addListener { _, _, _ -> repaint() }
        heightProperty().addListener { _, _, _ -> repaint() }

        object: AnimationTimer(){
            override fun handle(now: Long) {
                if(needsBlit.get()) {
                    NodeHelper.markDirty(this@MultiThreadSharedImpl, DirtyBits.NODE_BOUNDS)
                    NodeHelper.markDirty(this@MultiThreadSharedImpl, DirtyBits.REGION_SHAPE)
                }
            }
        }.start()
    }

    private fun initializeGL(){
        fxContext = GLContext.current()
        parallelContext = GLContext.create(fxContext!!, profile == GLProfile.Core)
        resultContext = GLContext.create(fxContext!!, profile == GLProfile.Core)

        thread(isDaemon = true) {
            parallelContext!!.makeCurrent()

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

            updateDrawFramebufferSize()
            fireReshapeEvent(lastDrawSize.first, lastDrawSize.second)
        }

        glViewport(0, 0, lastDrawSize.first, lastDrawSize.second)
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

            if (scaledWidth.toInt() != lastResultSize.first || scaledHeight.toInt() != lastResultSize.second) {
                lastResultSize = Pair(scaledWidth.toInt(), scaledHeight.toInt())
                updateResultFramebufferSize()
            }
            glViewport(0, 0, lastResultSize.first, lastResultSize.second)

            synchronized(blitLock){
                passthroughShader.copy(interThreadFBO, resultFBO)
            }
            fxContext!!.makeCurrent()
        }

        if(this::fxTexture.isInitialized)
            drawResultTexture(g, fxTexture)
    }

    private fun updateResultFramebufferSize() {
        if(::resultFBO.isInitialized)
            resultFBO.delete()

        val width = lastResultSize.first
        val height = lastResultSize.second

        // Create JavaFX texture
        if(::fxTexture.isInitialized)
            fxTexture.dispose()
        fxTexture = GraphicsPipeline.getDefaultResourceFactory()
            .createTexture(PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE, width, height)
        fxTexture.makePermanent()

        // Create framebuffer that connected to JavaFX's texture
        resultFBO = Framebuffer(width, height, existingTexture = fxTexture.GLTextureId)
    }

    private fun updateDrawFramebufferSize() {
        if(::fbo.isInitialized){
            fbo.delete()
            if(msaa != 0) msaaFBO.delete()
        }

        val width = lastDrawSize.first
        val height = lastDrawSize.second

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

    override fun dispose() {
        super.dispose()
        GLContext.delete(parallelContext!!)
        GLContext.delete(resultContext!!)
    }
}