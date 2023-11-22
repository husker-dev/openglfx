package com.huskerdev.openglfx.image

import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glBindFramebuffer
import com.huskerdev.openglfx.GLExecutor.Companion.glBindTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glGenTextures
import com.huskerdev.openglfx.GLExecutor.Companion.glGetInteger
import com.huskerdev.openglfx.GLExecutor.Companion.glReadPixels
import com.huskerdev.openglfx.GLExecutor.Companion.glTexImage2D
import com.huskerdev.openglfx.internal.fbo.Framebuffer
import javafx.scene.image.Image
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import java.nio.ByteBuffer


class GLImageManager {
    companion object {

        @JvmStatic
        fun toGL(image: Image, x: Int, y: Int, width: Int, height: Int): Framebuffer {
            val buffer = ByteBuffer.allocateDirect(width * height * 4)
            image.pixelReader.getPixels(x, y, width, height, PixelFormat.getByteBgraPreInstance(), buffer, width * 4)

            val texture = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, texture)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_BGRA, GL_UNSIGNED_BYTE, buffer)

            return Framebuffer(width, height, existingTexture = texture)
        }

        @JvmStatic
        fun fromGL(fbo: Int, x: Int, y: Int, width: Int, height: Int): Image {
            val oldDrawBuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING)
            val oldReadBuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING)

            glBindFramebuffer(GL_FRAMEBUFFER, fbo)
            val buffer = ByteBuffer.allocateDirect(width * height * 4)
            glReadPixels(x, y, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer)

            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oldDrawBuffer)
            glBindFramebuffer(GL_READ_FRAMEBUFFER, oldReadBuffer)

            return WritableImage(PixelBuffer(width, height, buffer, PixelFormat.getByteBgraPreInstance()))
        }

        @JvmStatic
        fun toGL(image: Image) =
            toGL(image, 0, 0, image.width.toInt(), image.height.toInt())
        @JvmStatic
        fun toGL(image: Image, width: Int, height: Int) =
            toGL(image, 0, 0, width, height)
        @JvmStatic
        fun fromGL(fbo: Int, width: Int, height: Int) =
            fromGL(fbo, 0, 0, width, height)
    }
}