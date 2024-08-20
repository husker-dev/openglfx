package com.huskerdev.openglfx.internal

import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindRenderbuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glBlitFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glCopyTexSubImage2D
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteFramebuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteRenderbuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glFramebufferRenderbuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glFramebufferTexture2D
import com.huskerdev.openglfx.GLExecutor.Companion.glGenFramebuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glGenRenderbuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glGetInteger
import com.huskerdev.openglfx.GLExecutor.Companion.glReadPixels
import com.huskerdev.openglfx.GLExecutor.Companion.glRenderbufferStorage
import com.huskerdev.openglfx.GLExecutor.Companion.glRenderbufferStorageMultisample
import com.huskerdev.openglfx.GLExecutor.Companion.glTexImage2D
import com.huskerdev.openglfx.GLExecutor.Companion.glTexParameteri
import java.nio.ByteBuffer
import kotlin.math.min

abstract class Framebuffer(
    val width: Int,
    val height: Int
) {

    var id: Int = 0
    var depthRenderbuffer: Int = 0

    class Default(
        width: Int,
        height: Int,
        texture: Int = -1,
        textureType: Int = GL_TEXTURE_2D,
        textureCreationFormat: Int = GL_RGBA    // Used when texture is -1
    ): Framebuffer(width, height){
        val texture: Int

        init {
            val oldDrawBuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING)
            val oldReadBuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING)

            id = glGenFramebuffers()
            glBindFramebuffer(GL_FRAMEBUFFER, id)

            if(texture == -1) {
                this.texture = glGenTextures()
                glBindTexture(textureType, this.texture)
                glTexImage2D(
                    textureType, 0,
                    textureCreationFormat, width, height, 0,
                    textureCreationFormat, GL_UNSIGNED_BYTE, null)
                glTexParameteri(textureType, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            }else {
                this.texture = texture
                glBindTexture(textureType, this.texture)
            }

            glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0, textureType, this.texture, 0)

            depthRenderbuffer = glGenRenderbuffers()
            glBindRenderbuffer(GL_RENDERBUFFER, depthRenderbuffer)
            glRenderbufferStorage(
                GL_RENDERBUFFER,
                GL_DEPTH_COMPONENT, width, height)
            glFramebufferRenderbuffer(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER, depthRenderbuffer)

            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oldDrawBuffer)
            glBindFramebuffer(GL_READ_FRAMEBUFFER, oldReadBuffer)
        }

        fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, targetBuffer: ByteBuffer){
            val oldReadBuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING)

            glBindFramebuffer(GL_READ_FRAMEBUFFER, id)
            glReadPixels(x, y, width, height, format, type, targetBuffer)
            glBindFramebuffer(GL_READ_FRAMEBUFFER, oldReadBuffer)
        }

        override fun delete() {
            super.delete()
            glDeleteTextures(texture)
        }
    }

    class MultiSampled(
        width: Int,
        height: Int,
        requestedSamples: Int
    ): Framebuffer(
        width, height
    ) {
        val samples: Int = min(glGetInteger(GL_MAX_SAMPLES), requestedSamples)
        val colorRenderBuffer: Int

        init {
            id = glGenFramebuffers()
            glBindFramebuffer(GL_FRAMEBUFFER, id)

            depthRenderbuffer = glGenRenderbuffers()
            glBindRenderbuffer(GL_RENDERBUFFER, depthRenderbuffer)
            glRenderbufferStorageMultisample(
                GL_RENDERBUFFER, this.samples,
                GL_DEPTH_COMPONENT, width, height)
            glFramebufferRenderbuffer(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER, depthRenderbuffer)

            colorRenderBuffer = glGenRenderbuffers()
            glBindRenderbuffer(GL_RENDERBUFFER, colorRenderBuffer)
            glRenderbufferStorageMultisample(
                GL_RENDERBUFFER, this.samples,
                GL_RGBA8, width, height)
            glFramebufferRenderbuffer(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0,
                GL_RENDERBUFFER, colorRenderBuffer)

            glBindFramebuffer(GL_FRAMEBUFFER, 0)
        }

        override fun delete() {
            super.delete()
            glDeleteRenderbuffers(colorRenderBuffer)
        }
    }


    fun bindFramebuffer(){
        glBindFramebuffer(GL_FRAMEBUFFER, id)
    }

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

    fun blitTo(fbo: Framebuffer) = blitTo(fbo.id)

    fun copyToTexture(texture: Int){
        val oldReadBuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, id)

        glBindTexture(GL_TEXTURE_2D, texture)
        glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height)

        glBindFramebuffer(GL_READ_FRAMEBUFFER, oldReadBuffer)
    }

    open fun delete(){
        glDeleteFramebuffers(id)
        glDeleteRenderbuffers(depthRenderbuffer)
    }
}