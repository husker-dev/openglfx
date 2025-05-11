package com.huskerdev.openglfx.internal.platforms


class VkExtMemory{

    @Suppress("unused")
    companion object {
        @JvmStatic private external fun nLoadFunctions()
        @JvmStatic private external fun nCreateVk(): LongArray
        @JvmStatic private external fun nCreateExternalImage(device: Long, physicalDevice: Long, width: Int, height: Int): LongArray
        @JvmStatic private external fun nFreeImage(device: Long, image: Long, memory: Long)
        @JvmStatic private external fun nDestroyVkDevice(device: Long)
        @JvmStatic private external fun nDestroyVkInstance(instance: Long)

        @JvmStatic private external fun vkGetMemoryFdKHR(device: Long, memory: Long): Int
        @JvmStatic private external fun vkGetMemoryWin32HandleKHR(device: Long, memory: Long): Long

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
                .run { ExternalImage(this@Vk, width, height, this[0], this[1], this[2]) }

        fun destroy() {
            nDestroyVkDevice(vkDevice)
            nDestroyVkInstance(vkInstance)
        }
    }

    data class ExternalImage(
        val vk: Vk,
        val width: Int,
        val height: Int,
        val vkImage: Long,
        val vkDeviceMemory: Long,
        val size: Long
    ) {
        fun dispose() =
            nFreeImage(vk.vkDevice, vkImage, vkDeviceMemory)

        fun createMemoryFd() =
            vkGetMemoryFdKHR(vk.vkDevice, vkDeviceMemory)

        fun getMemoryWin32Handle() =
            vkGetMemoryWin32HandleKHR(vk.vkDevice, vkDeviceMemory)
    }
}