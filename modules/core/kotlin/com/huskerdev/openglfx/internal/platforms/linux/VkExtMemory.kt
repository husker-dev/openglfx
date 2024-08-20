package com.huskerdev.openglfx.internal.platforms.linux


class VkExtMemory{
    companion object {
        @JvmStatic private external fun nLoadFunctions()
        @JvmStatic private external fun nCreateVk(): LongArray
        @JvmStatic private external fun nCreateExternalImage(device: Long, physicalDevice: Long, width: Int, height: Int): LongArray
        @JvmStatic private external fun nFreeImage(device: Long, image: Long, memory: Long)
        @JvmStatic private external fun nFreeVkInstance(instance: Long)

        init {
            nLoadFunctions()
        }

        fun createVk() =
            nCreateVk().run { Vk(this[0], this[1], this[2]) }
    }

    data class Vk(
        val vkInstance: Long,
        val vkPhysicalDevice: Long,
        val vkDevice: Long
    ){
        fun createExternalImage(width: Int, height: Int) =
            nCreateExternalImage(vkDevice, vkPhysicalDevice, width, height)
                .run { ExternalImage(this@Vk, this[0], this[1], this[2].toInt(),  this[3].toInt(), this[4]) }

        fun dispose() =
            nFreeVkInstance(vkInstance)
    }

    data class ExternalImage(
        val vk: Vk,
        val vkImage: Long,
        val vkDeviceMemory: Long,
        val fd1: Int,
        val fd2: Int,
        val size: Long
    ) {
        fun dispose() =
            nFreeImage(vk.vkDevice, vkImage, vkDeviceMemory)
    }
}