#ifndef OPENGLFX_MACOS_H
#define OPENGLFX_MACOS_H

#include <openglfx.h>

#define jni_win_iosurface(returnType, fun) extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_platforms_macos_IOSurface_##fun

#endif