package com.huskerdev.openglfx.utils

import com.sun.prism.GraphicsPipeline
import com.sun.prism.RTTexture
import com.sun.prism.Texture
import sun.misc.Unsafe
import java.nio.ByteBuffer


class OpenGLFXUtils {

    companion object {
        private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }[null] as Unsafe

        val pipelineName: String
            get() = GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]

        fun cleanByteBuffer(buffer: ByteBuffer) = unsafe.invokeCleaner(buffer)


        private val arch = System.getProperty("os.arch")
        val arm64 = arch.startsWith("aarch64") || arch.startsWith("armv8")
        val arm32 = arch.startsWith("arm") && !arm64
        val x64 = arch.contains("64") && !arm32 && !arm64
        val x86 = !x64 && !arm32 && !arm64

        val archName: String
            get() = if(arm64) "arm64"
                else if(arm32) "arm32"
                else if(x64) "x64"
                else if(x86) "x86"
                else throw UnsupportedOperationException("Unsupported OS architecture")

        val Texture.DX9TextureResource: Long
            get() = Class.forName("com.sun.prism.d3d.D3DTexture")
                .getMethod("getNativeSourceHandle")
                .apply { isAccessible = true }
                .invoke(this) as Long

        val Texture.GLTextureId: Int
            get() = Class.forName("com.sun.prism.es2.ES2RTTexture")
                .getMethod("getNativeSourceHandle")
                .apply { isAccessible = true }
                .invoke(this) as Int
    }
}