#ifndef __APPLE__

#include "openglfx.h"
#include <vulkan/vulkan.h>


#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
    #include <windows.h>
    #include <vulkan/vulkan_win32.h>

    static HMODULE libVk;

    #define EXTERNAL_MEMORY_PLATFORM_EXTENSION_NAME VK_KHR_EXTERNAL_MEMORY_WIN32_EXTENSION_NAME
    #define EXTERNAL_MEMORY_HANDLE_TYPE_PLATFORM_BIT VK_EXTERNAL_MEMORY_HANDLE_TYPE_D3D11_TEXTURE_KMT_BIT

#elif defined(__linux__)
    #include <dlfcn.h>
    static void* libVk;

    #define EXTERNAL_MEMORY_PLATFORM_EXTENSION_NAME VK_KHR_EXTERNAL_MEMORY_FD_EXTENSION_NAME
    #define EXTERNAL_MEMORY_HANDLE_TYPE_PLATFORM_BIT VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_FD_BIT_KHR
#endif


static void* vkGetProcAddress(const char* name){
#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
    if(libVk == NULL)
        libVk = LoadLibraryW(L"vulkan-1.dll");
    return GetProcAddress(libVk, name);

#elif defined(__linux__)
    if(libVk == NULL){
        static const char *NAMES[] = {"libvulkan.so.1", "libvulkan.so"};
        for(int i = 0; i < 2; i++)
            if((libVk = dlopen(NAMES[i], RTLD_NOW | RTLD_GLOBAL)) != NULL)
                break;
    }
    return dlsym(libVk, name);
#else
    return 0;
#endif
}

static PFN_vkCreateInstance _vkCreateInstance;
static PFN_vkEnumeratePhysicalDevices _vkEnumeratePhysicalDevices;
static PFN_vkGetPhysicalDeviceQueueFamilyProperties _vkGetPhysicalDeviceQueueFamilyProperties;
static PFN_vkCreateDevice _vkCreateDevice;
static PFN_vkGetPhysicalDeviceMemoryProperties _vkGetPhysicalDeviceMemoryProperties;
static PFN_vkCreateImage _vkCreateImage;
static PFN_vkGetImageMemoryRequirements _vkGetImageMemoryRequirements;
static PFN_vkAllocateMemory _vkAllocateMemory;
static PFN_vkBindImageMemory _vkBindImageMemory;
static PFN_vkGetDeviceProcAddr _vkGetDeviceProcAddr;
static PFN_vkDestroyImage _vkDestroyImage;
static PFN_vkFreeMemory _vkFreeMemory;
static PFN_vkDestroyInstance _vkDestroyInstance;
static PFN_vkDestroyDevice _vkDestroyDevice;


static unsigned int findMemoryType(VkPhysicalDevice physicalDevice, unsigned int typeFilter, VkMemoryPropertyFlags properties) {
    VkPhysicalDeviceMemoryProperties memProperties;
    _vkGetPhysicalDeviceMemoryProperties(physicalDevice, &memProperties);

    for (unsigned int i = 0; i < memProperties.memoryTypeCount; i++)
        if ((typeFilter & (1 << i)) && (memProperties.memoryTypes[i].propertyFlags & properties) == properties)
            return i;
    return 0;
}


jni_vkextmemory(void, nLoadFunctions)(JNIEnv* env, jobject) {
    _vkCreateInstance = (PFN_vkCreateInstance)vkGetProcAddress("vkCreateInstance");
    _vkEnumeratePhysicalDevices = (PFN_vkEnumeratePhysicalDevices)vkGetProcAddress("vkEnumeratePhysicalDevices");
    _vkGetPhysicalDeviceQueueFamilyProperties = (PFN_vkGetPhysicalDeviceQueueFamilyProperties)vkGetProcAddress("vkGetPhysicalDeviceQueueFamilyProperties");
    _vkCreateDevice = (PFN_vkCreateDevice)vkGetProcAddress("vkCreateDevice");
    _vkGetPhysicalDeviceMemoryProperties = (PFN_vkGetPhysicalDeviceMemoryProperties)vkGetProcAddress("vkGetPhysicalDeviceMemoryProperties");
    _vkCreateImage = (PFN_vkCreateImage)vkGetProcAddress("vkCreateImage");
    _vkGetImageMemoryRequirements = (PFN_vkGetImageMemoryRequirements)vkGetProcAddress("vkGetImageMemoryRequirements");
    _vkAllocateMemory = (PFN_vkAllocateMemory)vkGetProcAddress("vkAllocateMemory");
    _vkBindImageMemory = (PFN_vkBindImageMemory)vkGetProcAddress("vkBindImageMemory");
    _vkGetDeviceProcAddr = (PFN_vkGetDeviceProcAddr)vkGetProcAddress("vkGetDeviceProcAddr");
    _vkDestroyImage = (PFN_vkDestroyImage)vkGetProcAddress("vkDestroyImage");
    _vkFreeMemory = (PFN_vkFreeMemory)vkGetProcAddress("vkFreeMemory");
    _vkDestroyInstance = (PFN_vkDestroyInstance)vkGetProcAddress("vkDestroyInstance");
    _vkDestroyDevice = (PFN_vkDestroyDevice)vkGetProcAddress("vkDestroyDevice");
}

jni_vkextmemory(jlongArray, nCreateVk)(JNIEnv* env, jobject) {
    const char* instanceExtensions[] {
        VK_KHR_EXTERNAL_MEMORY_CAPABILITIES_EXTENSION_NAME
    };

    const char* deviceExtensions[] {
        VK_KHR_EXTERNAL_MEMORY_EXTENSION_NAME,
        EXTERNAL_MEMORY_PLATFORM_EXTENSION_NAME
    };

    // Create instance
    VkApplicationInfo appInfo{};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "openglfx";
    appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.pEngineName = "openglfx";
    appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.apiVersion = VK_API_VERSION_1_0;

    VkInstanceCreateInfo instanceCreateInfo{};
    instanceCreateInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    instanceCreateInfo.pApplicationInfo = &appInfo;
    instanceCreateInfo.enabledLayerCount = 0;
    instanceCreateInfo.enabledExtensionCount = 1;
    instanceCreateInfo.ppEnabledExtensionNames = instanceExtensions;

    VkInstance instance = NULL;
    _vkCreateInstance(&instanceCreateInfo, nullptr, &instance);

    // Get first GPU
    unsigned int gpu_count;
    _vkEnumeratePhysicalDevices(instance, &gpu_count, NULL);

    VkPhysicalDevice* devices = new VkPhysicalDevice[gpu_count];
    _vkEnumeratePhysicalDevices(instance, &gpu_count, devices);

    VkPhysicalDevice physicalDevice = devices[0];
    delete devices;

    // Find queue index
    unsigned int queueFamilyCount = 0;
    _vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyCount, NULL);

    VkQueueFamilyProperties* queueFamilies = new VkQueueFamilyProperties[queueFamilyCount];
    _vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyCount, queueFamilies);

    int queueIndex = 0;
    for (unsigned int i = 0; i < queueFamilyCount; i++) {
        VkQueueFamilyProperties queueFamily = queueFamilies[i];
        if (queueFamily.queueFlags & VK_QUEUE_GRAPHICS_BIT) {
            queueIndex = i;
            break;
        }
    }
    delete[] queueFamilies;

    // Create queue
    VkDeviceQueueCreateInfo queueCreateInfo{};
    queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueCreateInfo.queueFamilyIndex = queueIndex;
    queueCreateInfo.queueCount = 1;
    float queuePriority = 1.0f;
    queueCreateInfo.pQueuePriorities = &queuePriority;

    // Create vulkan device
    VkPhysicalDeviceFeatures deviceFeatures{};
    VkDeviceCreateInfo deviceCreateInfo{};
    deviceCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
    deviceCreateInfo.pQueueCreateInfos = &queueCreateInfo;
    deviceCreateInfo.queueCreateInfoCount = 1;
    deviceCreateInfo.pEnabledFeatures = &deviceFeatures;
    deviceCreateInfo.enabledLayerCount = 0;
    deviceCreateInfo.enabledExtensionCount = 2;
    deviceCreateInfo.ppEnabledExtensionNames = deviceExtensions;

    VkDevice device;
    _vkCreateDevice(physicalDevice, &deviceCreateInfo, nullptr, &device);

    return createLongArray(env, {
        (jlong) instance,
        (jlong) physicalDevice,
        (jlong) device
    });
}

jni_vkextmemory(jlongArray, nCreateExternalImage)(JNIEnv* env, jobject, jlong _device, jlong _physicalDevice, jint width, jint height) {
    VkDevice device = (VkDevice)_device;
    VkPhysicalDevice physicalDevice = (VkPhysicalDevice)_physicalDevice;

    // Create image
    VkExternalMemoryImageCreateInfo extImageCreateInfo{};
    extImageCreateInfo.sType = VK_STRUCTURE_TYPE_EXTERNAL_MEMORY_IMAGE_CREATE_INFO;
    extImageCreateInfo.handleTypes = EXTERNAL_MEMORY_HANDLE_TYPE_PLATFORM_BIT;

    VkImageCreateInfo imageCreateInfo{};
    imageCreateInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
    imageCreateInfo.imageType = VK_IMAGE_TYPE_2D;
    imageCreateInfo.extent.width = width;
    imageCreateInfo.extent.height = height;
    imageCreateInfo.extent.depth = 1;
    imageCreateInfo.mipLevels = 1;
    imageCreateInfo.arrayLayers = 1;
    imageCreateInfo.format = VK_FORMAT_R8G8B8A8_SRGB;
    imageCreateInfo.usage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT;
    imageCreateInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
    imageCreateInfo.pNext = &extImageCreateInfo;

    VkImage textureImage;
    _vkCreateImage(device, &imageCreateInfo, nullptr, &textureImage);

    // Allocate memory buffer
    VkExportMemoryAllocateInfo exportMemoryAllocateInfo{};
    exportMemoryAllocateInfo.sType = VK_STRUCTURE_TYPE_EXPORT_MEMORY_ALLOCATE_INFO;
    exportMemoryAllocateInfo.handleTypes = EXTERNAL_MEMORY_HANDLE_TYPE_PLATFORM_BIT;

    VkMemoryRequirements memRequirements;
    _vkGetImageMemoryRequirements(device, textureImage, &memRequirements);

    VkMemoryAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
    allocInfo.allocationSize = memRequirements.size;
    allocInfo.memoryTypeIndex = findMemoryType(physicalDevice, memRequirements.memoryTypeBits, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    allocInfo.pNext = &exportMemoryAllocateInfo;

    VkDeviceMemory textureImageMemory;
    _vkAllocateMemory(device, &allocInfo, nullptr, &textureImageMemory);

    return createLongArray(env, {
        (jlong) textureImage,
        (jlong) textureImageMemory,
        (jlong) memRequirements.size
    });
}

jni_vkextmemory(jint, vkGetMemoryFdKHR)(JNIEnv* env, jobject, jlong _device, jlong _memory) {
    VkDevice device = (VkDevice)_device;
    VkDeviceMemory textureImageMemory = (VkDeviceMemory)_memory;

    PFN_vkGetMemoryFdKHR _vkGetMemoryFdKHR = (PFN_vkGetMemoryFdKHR)_vkGetDeviceProcAddr(device, "vkGetMemoryFdKHR");

    int fd;
    VkMemoryGetFdInfoKHR getFdInfo{};
    getFdInfo.sType = VK_STRUCTURE_TYPE_MEMORY_GET_FD_INFO_KHR;
    getFdInfo.memory = textureImageMemory;
    getFdInfo.handleType = EXTERNAL_MEMORY_HANDLE_TYPE_PLATFORM_BIT;
    _vkGetMemoryFdKHR(device, &getFdInfo, &fd);

    return fd;
}

#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
jni_vkextmemory(jlong, vkGetMemoryWin32HandleKHR)(JNIEnv* env, jobject, jlong _device, jlong _memory) {
    VkDevice device = (VkDevice)_device;
    VkDeviceMemory textureImageMemory = (VkDeviceMemory)_memory;

    PFN_vkGetMemoryWin32HandleKHR _vkGetMemoryWin32HandleKHR = (PFN_vkGetMemoryWin32HandleKHR)_vkGetDeviceProcAddr(device, "vkGetMemoryWin32HandleKHR");

    HANDLE handle = NULL;
    VkMemoryGetWin32HandleInfoKHR getWin32HandleInfo{};
    getWin32HandleInfo.sType = VK_STRUCTURE_TYPE_MEMORY_GET_WIN32_HANDLE_INFO_KHR;
    getWin32HandleInfo.memory = textureImageMemory;
    getWin32HandleInfo.handleType = EXTERNAL_MEMORY_HANDLE_TYPE_PLATFORM_BIT;
    _vkGetMemoryWin32HandleKHR(device, &getWin32HandleInfo, &handle);

    return (jlong)handle;
}
#endif

jni_vkextmemory(void, nFreeImage)(JNIEnv* env, jobject, jlong _device, jlong image, jlong memory) {
    VkDevice device = (VkDevice)_device;

    _vkDestroyImage(device, (VkImage)image, NULL);
    _vkFreeMemory(device, (VkDeviceMemory)memory, NULL);
}

jni_vkextmemory(void, nDestroyVkDevice)(JNIEnv* env, jobject, jlong _device) {
    VkDevice device = (VkDevice)_device;

    _vkDestroyDevice(device, nullptr);
}

jni_vkextmemory(void, nDestroyVkInstance)(JNIEnv* env, jobject, jlong _instance) {
    VkInstance instance = (VkInstance)_instance;

    _vkDestroyInstance(instance, nullptr);
}

#endif