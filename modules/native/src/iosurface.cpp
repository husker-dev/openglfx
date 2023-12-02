//
// Created by Nikita Shtengauer on 20.11.2023.
//

#include <jni.h>
#include <CoreFoundation/CoreFoundation.h>
#include <CoreGraphics/CGDisplayConfiguration.h>
#include <IOSurface/IOSurface.h>
#include <OpenGL/OpenGL.h>
#include "openglfx.h"


iosfun(jdouble, nGetDisplayDPI)(JNIEnv *, jobject, jdouble x, jdouble y) {
    CGDirectDisplayID display;
    CGGetDisplaysWithPoint(CGPoint{x, y}, 1, &display, nullptr);

    CFStringRef keys[1]{
        kCGDisplayShowDuplicateLowResolutionModes
    };
    CFBooleanRef values[1]{
        kCFBooleanTrue
    };
    CFDictionaryRef dictionary = CFDictionaryCreate(
                nullptr, (const void **) keys, (const void **) values, 1,
                &kCFCopyStringDictionaryKeyCallBacks,
                &kCFTypeDictionaryValueCallBacks);

    CFArrayRef modes = CGDisplayCopyAllDisplayModes(display, dictionary);
    CFRelease(dictionary);

    int actualWidth = 0;
    for(int i = 0; i < CFArrayGetCount(modes); i++){
        CGDisplayModeRef mode = (CGDisplayModeRef)CFArrayGetValueAtIndex(modes, i);

        int width = CGDisplayModeGetPixelWidth(mode);
        if(width == CGDisplayModeGetWidth(mode) && actualWidth < width)
            actualWidth = width;
    }
    return (double)actualWidth / CGDisplayPixelsWide(display);
}

iosfun(jlong, nCreateIOSurface)(JNIEnv *, jobject, jint width, jint height) {
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

iosfun(void, nReleaseIOSurface)(JNIEnv *, jobject, jlong ioSurfaceRef) {
    CFRelease((IOSurfaceRef) ioSurfaceRef);
}

iosfun(jint, nCGLTexImageIOSurface2D)(JNIEnv *, jobject,
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

iosfun(void, nIOSurfaceLock)(JNIEnv *, jobject, jlong ioSurfaceRef) {
    IOSurfaceLock((IOSurfaceRef) ioSurfaceRef, kIOSurfaceLockReadOnly, nullptr);
}

iosfun(void, nIOSurfaceUnlock)(JNIEnv *, jobject, jlong ioSurfaceRef) {
    IOSurfaceUnlock((IOSurfaceRef) ioSurfaceRef, kIOSurfaceLockReadOnly, nullptr);
}