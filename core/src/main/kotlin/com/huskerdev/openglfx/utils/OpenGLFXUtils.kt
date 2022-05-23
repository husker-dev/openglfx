package com.huskerdev.openglfx.utils

import com.sun.javafx.PlatformUtil
import com.sun.prism.GraphicsPipeline
import com.sun.prism.Texture
import sun.misc.Unsafe
import java.io.FileOutputStream
import java.nio.ByteBuffer


class OpenGLFXUtils {

    companion object {
        private var libraryLoaded = false
        private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }[null] as Unsafe

        val pipelineName: String
            get() = GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]

        private val arch = System.getProperty("os.arch")
        val archName: String
            get() = if(arch.startsWith("aarch64") || arch.startsWith("armv8")) "arm64"
                else if(arch.startsWith("arm")) "arm32"
                else if(arch.contains("64")) "x64"
                else "x86"
        val osName = if(PlatformUtil.isWindows()) "win" else if(PlatformUtil.isLinux()) "linux" else "macos"
        val osLibExtension = if(PlatformUtil.isWindows()) "dll" else if(PlatformUtil.isLinux()) "so" else "dylib"


        fun cleanByteBuffer(buffer: ByteBuffer) = unsafe.invokeCleaner(buffer)

        fun loadLibrary(){
            if(libraryLoaded) return
            libraryLoaded = true

            val fileName = "${osName}_utils_${archName}.$osLibExtension"

            val tmpFileName = "${System.getProperty("java.io.tmpdir")}/$fileName"
            try {
                val inputStream = this::class.java.getResourceAsStream("/com/huskerdev/openglfx/natives/$fileName")!!
                FileOutputStream(tmpFileName).use {
                    inputStream.transferTo(it)
                }
            }catch (_: Exception){}
            System.load(tmpFileName)
        }

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