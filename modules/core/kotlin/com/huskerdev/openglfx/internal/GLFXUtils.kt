package com.huskerdev.openglfx.internal

import com.huskerdev.grapl.core.platform.Platform
import com.huskerdev.openglfx.GLExecutor.Companion.glGetInteger
import com.huskerdev.openglfx.GLFXInfo
import com.huskerdev.openglfx.GL_TEXTURE_BINDING_2D
import com.huskerdev.openglfx.internal.platforms.win.D3D9
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import com.sun.scenario.Settings
import javafx.scene.Node
import java.nio.ByteBuffer
import java.util.function.Consumer


class GLFXUtils {

    companion object {
        @JvmStatic external fun createDirectBuffer(size: Int): ByteBuffer
        @JvmStatic external fun cleanDirectBuffer(buffer: ByteBuffer)

        fun loadLibrary() =
            Platform.loadLibraryFromResources("com/huskerdev/openglfx/native", "lib", GLFXInfo.VERSION)

        fun getDPI(node: Node) =
            if(node.scene == null || node.scene.window == null)
                1.0
            else
                node.scene.window.outputScaleY

        fun createPermanentFXTexture(width: Int, height: Int): Texture {
            val texture = GraphicsPipeline.getDefaultResourceFactory()
                .createTexture(
                    PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE,
                    width, height
                )
            texture.makePermanent()
            return texture
        }

        fun createPermanentFXRTTexture(width: Int, height: Int): Texture {
            val texture = GraphicsPipeline.getDefaultResourceFactory()
                .createRTTexture(
                    width, height, Texture.WrapMode.CLAMP_TO_EDGE,
                )
            texture.makePermanent()
            return texture
        }

        fun fetchGLTexId(texture: Texture, g: Graphics): Int{
            g.drawTexture(texture, 0f, 0f, 0f, 0f)
            return glGetInteger(GL_TEXTURE_BINDING_2D)
        }

        fun fetchDXTexHandle(texture: Texture, g: Graphics): Long{
            g.drawTexture(texture, 0f, 0f, 0f, 0f)
            return D3D9.Device.jfx.getTexture(0)
        }

        fun getPulseDuration(): Int {
            if (Settings.get("javafx.animation.framerate") != null) {
                val overrideHz = Settings.getInt("javafx.animation.framerate", 60)
                if (overrideHz > 0)
                    return overrideHz
            } else if (Settings.get("javafx.animation.pulse") != null) {
                val overrideHz = Settings.getInt("javafx.animation.pulse", 60)
                if (overrideHz > 0)
                    return overrideHz
            } else {
                val rate: Int = Toolkit.getToolkit().refreshRate
                if (rate > 0)
                    return rate
            }
            return 60
        }

        fun <T> List<Consumer<T>>.dispatchConsumer(event: T) {
            if(isNotEmpty()) forEach { it.accept(event) }
        }
        fun <T> List<(T) -> Unit>.dispatchEvent(event: T) {
            if(isNotEmpty()) forEach { it(event) }
        }
        fun List<() -> Unit>.dispatchEvent() {
            if(isNotEmpty()) forEach { it() }
        }
    }
}