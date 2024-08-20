#ifndef OPENGLFX_LINUX_H
#define OPENGLFX_LINUX_H

#include <openglfx.h>
#include <dlfcn.h>

#define jni_linux_vkextmemory(returnType, fun) extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_platforms_linux_VkExtMemory_##fun

#endif