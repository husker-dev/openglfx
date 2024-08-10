#include "openglfx-windows.h"



jni_win_wgldx(jlong, wglDXOpenDeviceNV)(JNIEnv* env, jobject, jlong dxDevice) {
    checkWGLFunctions();
    return (jlong)wglDXOpenDeviceNV((void*)dxDevice);
}

jni_win_wgldx(jboolean, wglDXCloseDeviceNV)(JNIEnv* env, jobject, jlong hDevice) {
    checkWGLFunctions();
    return (jboolean)wglDXCloseDeviceNV((HANDLE)hDevice);
}

jni_win_wgldx(jlong, wglDXRegisterObjectNV)(JNIEnv* env, jobject, jlong device, jlong dxResource, jint name, jint type, jint access) {
    checkWGLFunctions();
    return (jlong)wglDXRegisterObjectNV((HANDLE)device, (void*)dxResource, name, type, access);
}

jni_win_wgldx(jboolean, wglDXSetResourceShareHandleNV)(JNIEnv* env, jobject, jlong dxResource, jlong shareHandle) {
    checkWGLFunctions();
    return (jboolean)wglDXSetResourceShareHandleNV((void*)dxResource, (HANDLE)shareHandle);
}

jni_win_wgldx(jboolean, wglDXUnregisterObjectNV)(JNIEnv* env, jobject, jlong device, jlong object) {
    checkWGLFunctions();
    return (jboolean)wglDXUnregisterObjectNV((HANDLE)device, (HANDLE)object);
}

jni_win_wgldx(jboolean, wglDXLockObjectsNV)(JNIEnv* env, jobject, jlong handle, jlong textureHandle) {
    checkWGLFunctions();
    return wglDXLockObjectsNV((HANDLE)handle, 1, (HANDLE*)&textureHandle);
}

jni_win_wgldx(jboolean, wglDXUnlockObjectsNV)(JNIEnv* env, jobject, jlong handle, jlong textureHandle) {
    checkWGLFunctions();
    return wglDXUnlockObjectsNV((HANDLE)handle, 1, (HANDLE*)&textureHandle);
}