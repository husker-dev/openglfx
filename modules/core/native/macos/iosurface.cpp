//
// Created by Nikita Shtengauer on 20.11.2023.
//

#include "openglfx-macos.h"

#include <IOSurface/IOSurface.h>
#include <OpenGL/OpenGL.h>




jni_win_iosurface(jlong, nCreateIOSurface)(JNIEnv *, jobject, jint width, jint height) {
    int bytes = 4;
    CFStringRef keys[3]{
            kIOSurfaceWidth, kIOSurfaceHeight, kIOSurfaceBytesPerElement
    };
    CFNumberRef values[3]{
            CFNumberCreate(kCFAllocatorDefault, kCFNumberIntType, &width),
            CFNumberCreate(kCFAllocatorDefault, kCFNumberIntType, &height),
            CFNumberCreate(kCFAllocatorDefault, kCFNumberIntType, &bytes)
    };

    CFDictionaryRef dictionary = CFDictionaryCreate(
            nullptr,
            (const void **) keys, (const void **) values, 3,
            &kCFCopyStringDictionaryKeyCallBacks,
            &kCFTypeDictionaryValueCallBacks);
    IOSurfaceRef ioSurfaceRef = IOSurfaceCreate(dictionary);
    CFRelease(dictionary);
    return (jlong) ioSurfaceRef;
}

jni_win_iosurface(void, nReleaseIOSurface)(JNIEnv *, jobject, jlong ioSurfaceRef) {
    CFRelease((IOSurfaceRef) ioSurfaceRef);
}

jni_win_iosurface(jint, nCGLTexImageIOSurface2D)(JNIEnv *, jobject,
                                      jlong ctx, jint target, jint internalFormat, jint width, jint height, jint format,
                                      jint type,
                                      jlong ioSurfaceRef, jint plane) {
    return (jint) CGLTexImageIOSurface2D(
            (CGLContextObj) ctx,
            (GLenum) target, (GLenum) internalFormat,
            (GLsizei) width, (GLsizei) height,
            (GLenum) format, (GLenum) type,
            (IOSurfaceRef) ioSurfaceRef, (GLuint) plane);
}

jni_win_iosurface(void, nIOSurfaceLock)(JNIEnv *, jobject, jlong ioSurfaceRef) {
    IOSurfaceLock((IOSurfaceRef) ioSurfaceRef, kIOSurfaceLockReadOnly, nullptr);
}

jni_win_iosurface(void, nIOSurfaceUnlock)(JNIEnv *, jobject, jlong ioSurfaceRef) {
    IOSurfaceUnlock((IOSurfaceRef) ioSurfaceRef, kIOSurfaceLockReadOnly, nullptr);
}