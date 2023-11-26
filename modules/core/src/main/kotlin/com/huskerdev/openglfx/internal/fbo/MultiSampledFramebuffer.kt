package com.huskerdev.openglfx.internal.fbo

import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindRenderbuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBlitFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteFramebuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteRenderbuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glFramebufferRenderbuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glGenFramebuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glGenRenderbuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glGetInteger
import com.huskerdev.openglfx.GLExecutor.Companion.glRenderbufferStorageMultisample
import kotlin.math.min

class MultiSampledFramebuffer(
    requestedMSAA: Int,
    val width: Int,
    val height: Int
){
    val msaa: Int
    val id: Int
    private val depthRenderbuffer: Int
    private val colorRenderBuffer: Int

    init {
        this.msaa = min(glGetInteger(GL_MAX_SAMPLES), requestedMSAA)

        id = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, id)

        depthRenderbuffer = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, depthRenderbuffer)
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, this.msaa, GL_DEPTH_COMPONENT, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderbuffer)

        colorRenderBuffer = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, colorRenderBuffer)
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, this.msaa, GL_RGBA8, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorRenderBuffer)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun bindFramebuffer(){
        glBindFramebuffer(GL_FRAMEBUFFER, id)
    }

    fun delete() {
        glDeleteFramebuffers(id)
        glDeleteRenderbuffers(depthRenderbuffer)
        glDeleteRenderbuffers(colorRenderBuffer)
    }

    fun blitTo(fbo: Framebuffer) = blitTo(fbo.id)

    fun blitTo(fbo: Int){
        val oldDrawBuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING)
        val oldReadBuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING)

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbo)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, id)
        glBlitFramebuffer(
            0, 0, width, height,
            0, 0, width, height,
            GL_COLOR_BUFFER_BIT, GL_NEAREST
        )

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oldDrawBuffer)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, oldReadBuffer)
    }
}