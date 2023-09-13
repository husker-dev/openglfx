package com.huskerdev.openglfx.utils.fbo

import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindRenderbuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteFramebuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteRenderbuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glDeleteTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glFramebufferRenderbuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glFramebufferTexture2D
import com.huskerdev.openglfx.GLExecutor.Companion.glGenFramebuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glGenRenderbuffers
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glRenderbufferStorage
import com.huskerdev.openglfx.GLExecutor.Companion.glTexImage2D
import com.huskerdev.openglfx.GLExecutor.Companion.glTexParameteri

class Framebuffer(
    width: Int,
    height: Int,
    private val existingTexture: Int = -1
) {

    val id: Int
    val texture: Int
    val depthRenderbuffer: Int

    init {
        id = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, id)

        if(existingTexture == -1) {
            texture = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, texture)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0)
        }else texture = existingTexture

        depthRenderbuffer = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, depthRenderbuffer)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderbuffer)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun bindFramebuffer(){
        glBindFramebuffer(GL_FRAMEBUFFER, id)
    }

    fun delete(){
        if(existingTexture == -1)
            glDeleteTextures(texture)
        glDeleteFramebuffers(id)
        glDeleteRenderbuffers(depthRenderbuffer)
    }
}