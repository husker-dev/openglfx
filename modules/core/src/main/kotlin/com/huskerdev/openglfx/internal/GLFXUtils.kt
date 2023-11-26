package com.huskerdev.openglfx.internal

import com.huskerdev.ojgl.utils.OS
import com.huskerdev.ojgl.utils.PlatformUtils
import com.sun.javafx.tk.RenderJob
import com.sun.javafx.tk.Toolkit
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import sun.misc.Unsafe
import java.nio.Buffer
import java.nio.ByteBuffer
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask


internal class GLFXUtils {

    companion object {
        private var isLibLoaded = false

        private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }[null] as Unsafe

        fun loadLibrary(){
            if(isLibLoaded) return
            isLibLoaded = true

            val basename = "openglfx"
            val fileName = when(PlatformUtils.os) {
                OS.Windows, OS.Linux    -> "$basename-${PlatformUtils.arch}.${PlatformUtils.dynamicLibExt}"
                OS.MacOS                -> "$basename.dylib"
                else -> throw UnsupportedOperationException("Unsupported OS")
            }
            PlatformUtils.loadLibraryFromResources("/com/huskerdev/openglfx/natives/$fileName")
        }

        val Texture.D3DTextureResource: Long
            get() = Class.forName("com.sun.prism.d3d.D3DTexture")
                .getMethod("getNativeSourceHandle")
                .apply { isAccessible = true }
                .invoke(this) as Long

        val Texture.GLTextureId: Int
            get() = Class.forName("com.sun.prism.es2.ES2RTTexture")
                .getMethod("getNativeSourceHandle")
                .apply { isAccessible = true }
                .invoke(this) as Int

        fun createPermanentFXTexture(width: Int, height: Int): Texture {
            val texture = GraphicsPipeline.getDefaultResourceFactory()
                .createTexture(
                    PixelFormat.BYTE_BGRA_PRE, Texture.Usage.DYNAMIC, Texture.WrapMode.CLAMP_TO_EDGE,
                    width, height
                )
            texture.makePermanent()
            return texture
        }

        fun Texture.updateData(buffer: Buffer, width: Int, height: Int) =
            this.update(buffer, PixelFormat.BYTE_BGRA_PRE,
                0, 0, 0, 0, width, height, width * 4, true)

        fun ByteBuffer.dispose() =
            unsafe.invokeCleaner(this)

        fun runOnRenderThread(runnable: () -> Unit) {
            if (Thread.currentThread().name.startsWith("QuantumRenderer")) {
                runnable()
            } else {
                val task = FutureTask<Void?>(runnable, null)
                Toolkit.getToolkit().addRenderJob(RenderJob(task))
                try {
                    task.get()
                } catch (ex: ExecutionException) {
                    throw AssertionError(ex)
                } catch (_: InterruptedException) {}
            }
        }
    }
}