#include <openglfx.h>

#include <d3d9.h>


wglChoosePixelFormatARBPtr       wglChoosePixelFormatARB;
wglCreateContextAttribsARBPtr    wglCreateContextAttribsARB;
wglDXOpenDeviceNVPtr             wglDXOpenDeviceNV;
wglDXCloseDeviceNVPtr            wglDXCloseDeviceNV;
wglDXRegisterObjectNVPtr         wglDXRegisterObjectNV;
wglDXSetResourceShareHandleNVPtr wglDXSetResourceShareHandleNV;
wglDXUnregisterObjectNVPtr       wglDXUnregisterObjectNV;
wglDXLockObjectsNVPtr            wglDXLockObjectsNV;
wglDXUnlockObjectsNVPtr          wglDXUnlockObjectsNV;

nvdxfun(jlong, hasNVDXInteropFunctions)(JNIEnv* env, jobject) {
    return wglDXOpenDeviceNV != 0;
}

nvdxfun(jlong, wglDXOpenDeviceNV)(JNIEnv* env, jobject, jlong dxDevice) {
    return (jlong)wglDXOpenDeviceNV((void*)dxDevice);
}

nvdxfun(jboolean, wglDXCloseDeviceNV)(JNIEnv* env, jobject, jlong hDevice) {
    return (jboolean)wglDXCloseDeviceNV((HANDLE)hDevice);
}

nvdxfun(jlong, wglDXRegisterObjectNV)(JNIEnv* env, jobject, jlong device, jlong dxResource, jint name, jint type, jint access) {
    return (jlong)wglDXRegisterObjectNV((HANDLE)device, (void*)dxResource, name, type, access);
}

nvdxfun(jboolean, wglDXSetResourceShareHandleNV)(JNIEnv* env, jobject, jlong dxResource, jlong shareHandle) {
    return (jboolean)wglDXSetResourceShareHandleNV((void*)dxResource, (HANDLE)shareHandle);
}

nvdxfun(jboolean, wglDXUnregisterObjectNV)(JNIEnv* env, jobject, jlong device, jlong object) {
    return (jboolean)wglDXUnregisterObjectNV((HANDLE)device, (HANDLE)object);
}

nvdxfun(jboolean, wglDXLockObjectsNV)(JNIEnv* env, jobject, jlong handle, jlong textureHandle) {
    return wglDXLockObjectsNV((HANDLE)handle, 1, (HANDLE*)&textureHandle);
}

nvdxfun(jboolean, wglDXUnlockObjectsNV)(JNIEnv* env, jobject, jlong handle, jlong textureHandle) {
    return wglDXUnlockObjectsNV((HANDLE)handle, 1, (HANDLE*)&textureHandle);
}