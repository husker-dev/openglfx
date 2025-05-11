package com.huskerdev.openglfx.internal

import com.huskerdev.grapl.core.platform.Platform
import com.huskerdev.openglfx.*
import com.huskerdev.openglfx.GLExecutor.Companion.glActiveTexture
import com.huskerdev.openglfx.GLExecutor.Companion.glGetInteger
import com.huskerdev.openglfx.internal.platforms.win.D3D9
import com.sun.prism.Graphics
import com.sun.prism.GraphicsPipeline
import com.sun.prism.PixelFormat
import com.sun.prism.Texture
import javafx.scene.Node
import java.lang.reflect.Field
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer


class GLFXUtils {

    companion object {
        @JvmStatic external fun createDirectBuffer(size: Int): ByteBuffer
        @JvmStatic external fun cleanDirectBuffer(buffer: ByteBuffer)

        private val initialized = AtomicBoolean()

        private val unsafeClass by lazy {
            Class.forName("sun.misc.Unsafe")
        }
        private val unsafe by lazy {
            unsafeClass.getDeclaredField("theUnsafe").apply { trySetAccessible() }[null]
        }


        fun loadLibrary() {
            if(initialized.getAndSet(true))
                return
            Platform.loadLibraryFromResources("com/huskerdev/openglfx/natives", "lib", GLFXInfo.VERSION)

            // Uses modules
            if (System.getProperty("openglfx.disable.exports", "false") == "false" &&
                ModuleLayer.boot().findModule("javafx.graphics").isPresent
            ){
                arrayOf(
                    "com.sun.prism",
                    "com.sun.javafx.scene.layout",
                    "com.sun.javafx.scene",
                    "com.sun.javafx.sg.prism",
                    "com.sun.scenario",
                    "com.sun.javafx.tk",
                    "com.sun.glass.ui"
                ).forEach {
                    addExports("javafx.graphics", it, GLFXUtils::class.java.module)
                }
            }
        }

        private fun addExports(module: String, pkg: String, currentModule: Module) {
            try {
                val moduleOpt = ModuleLayer.boot().findModule(module)
                val addOpensMethodImpl = Module::class.java.getDeclaredMethod("implAddExports", String::class.java, Module::class.java)

                @Suppress("unused")
                class OffsetProvider(val first: Int)

                val firstFieldOffset = unsafeClass.getDeclaredMethod("objectFieldOffset", Field::class.java)
                    .invoke(unsafe, OffsetProvider::class.java.getDeclaredField("first"))

                unsafeClass.getDeclaredMethod("putBooleanVolatile", Object::class.java, Long::class.java, Boolean::class.java)
                    .invoke(unsafe, addOpensMethodImpl, firstFieldOffset, true)

                addOpensMethodImpl.invoke(moduleOpt.get(), pkg, currentModule)
            } catch (e: Throwable) {
                System.err.println("Could not add exports: $module/$pkg to ${currentModule.name}")
                e.printStackTrace()
            }
        }

        val pipeline: String
            get() {
                if(GraphicsPipeline.getPipeline() == null)
                    throw UnsupportedOperationException("Could not detect pipeline, make sure to initialize JavaFX")
                return GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]
            }

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
            val oldActiveTexture = glGetInteger(GL_ACTIVE_TEXTURE)
            glActiveTexture(GL_TEXTURE0)
            val textureId = glGetInteger(GL_TEXTURE_BINDING_2D)
            if (oldActiveTexture != GL_TEXTURE_2D) {
                glActiveTexture(oldActiveTexture)
            }
            return textureId
        }

        fun fetchDXTexHandle(texture: Texture, g: Graphics): Long{
            g.drawTexture(texture, 0f, 0f, 0f, 0f)
            return D3D9.Device.jfx.getTexture(0)
        }

        fun <T> List<Consumer<T>>.dispatchConsumer(event: T) {
            if(isNotEmpty()) forEach { it.accept(event) }
        }
    }
}