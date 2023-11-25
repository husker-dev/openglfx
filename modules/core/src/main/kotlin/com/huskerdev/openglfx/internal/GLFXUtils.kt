package com.huskerdev.openglfx.internal

import com.huskerdev.ojgl.utils.OS
import com.huskerdev.ojgl.utils.PlatformUtils
import com.sun.javafx.tk.PlatformImage
import com.sun.javafx.tk.Toolkit
import com.sun.prism.Texture
import com.sun.prism.impl.BaseTexture
import com.sun.prism.impl.ManagedResource
import javafx.scene.image.Image


internal class GLFXUtils {

    companion object {
        private var isLibLoaded = false

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

        fun Image.getPlatformImage() = Toolkit.getImageAccessor().getPlatformImage(this) as PlatformImage

        fun Texture.disposeManually(isManagedThread: Boolean = true){
            if(isManagedThread){
                this.dispose()
                return
            }

            // Override Texture.dispose() to use outside Managed thread
            if(this is BaseTexture<*>){
                val managedResource = this.getDeclaredField("resource", BaseTexture::class.java) as ManagedResource<*>

                val resourceField = managedResource.declaredField("resource", ManagedResource::class.java)
                val resource = resourceField[managedResource]

                if(resource != null){
                    managedResource.free()
                    managedResource.declaredField("disposalRequested", ManagedResource::class.java).set(managedResource, false)
                    resourceField.set(managedResource, null)
                    managedResource.pool::class.java.getMethod("resourceFreed", ManagedResource::class.java).invoke(managedResource.pool, managedResource)
                }
            }
        }

        private fun Any.declaredField(name: String, clazz: Class<*> = this::class.java) =
            clazz.getDeclaredField(name).apply { isAccessible = true }

        private fun Any.getDeclaredField(name: String, clazz: Class<*> = this::class.java) =
            clazz.getDeclaredField(name).apply { isAccessible = true }[this]
    }
}