#include "openglfx.h"

void* a_GetProcAddress(const char* name) {
#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
    if(libGL == NULL){
        libGL = LoadLibraryW(L"opengl32.dll");
        a_wglGetProcAddress = (wglGetProcAddressPtr)GetProcAddress(libGL, "wglGetProcAddress");
    }
    void* procAddr = a_wglGetProcAddress(name);
    if(procAddr == NULL)
        procAddr = GetProcAddress(libGL, name);
    return procAddr;

#elif defined(__linux__)
    if(libGL == NULL){
        static const char *NAMES[] = {"libGL.so.1", "libGL.so"};
        for(int i = 0; i < 2; i++)
            if((libGL = dlopen(NAMES[i], RTLD_NOW | RTLD_GLOBAL)) != NULL)
                break;
    }
    void* procAddr = (void*)glXGetProcAddressARB((GLubyte*)name);
    if(procAddr == NULL)
        procAddr = dlsym(libGL, name);
    return procAddr;

#elif defined(__APPLE__)
    if(libGL == NULL){
        static const char *NAMES[] = {
            "../Frameworks/OpenGL.framework/OpenGL",
            "/Library/Frameworks/OpenGL.framework/OpenGL",
            "/System/Library/Frameworks/OpenGL.framework/OpenGL",
            "/System/Library/Frameworks/OpenGL.framework/Versions/Current/OpenGL"
        };
        for(int i = 0; i < 4; i++)
            if((libGL = dlopen(NAMES[i], RTLD_NOW | RTLD_GLOBAL)) != NULL)
                break;
    }
    return dlsym(libGL, name);
#endif
}

extern "C" {

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_nInitGLFunctions(JNIEnv* env, jobject) {
    a_glViewport = (glViewportPtr)a_GetProcAddress("glViewport");
    a_glTexParameteri = (glTexParameteri)a_GetProcAddress("glTexParameteri");
    a_glTexImage2D = (glTexImage2DPtr)a_GetProcAddress("glTexImage2D");
    a_glReadPixels = (glReadPixelsPtr)a_GetProcAddress("glReadPixels");
    a_glFramebufferRenderbuffer = (glFramebufferRenderbufferPtr)a_GetProcAddress("glFramebufferRenderbuffer");
    a_glRenderbufferStorage = (glRenderbufferStoragePtr)a_GetProcAddress("glRenderbufferStorage");
    a_glFramebufferTexture2D = (glFramebufferTexture2DPtr)a_GetProcAddress("glFramebufferTexture2D");
    a_glBindTexture = (glBindTexturePtr)a_GetProcAddress("glBindTexture");
    a_glBindRenderbuffer = (glBindRenderbufferPtr)a_GetProcAddress("glBindRenderbuffer");
    a_glBindFramebuffer = (glBindFramebufferPtr)a_GetProcAddress("glBindFramebuffer");
    a_glGenTextures = (glGenTexturesPtr)a_GetProcAddress("glGenTextures");
    a_glGenRenderbuffers = (glGenRenderbuffersPtr)a_GetProcAddress("glGenRenderbuffers");
    a_glGenFramebuffers = (glGenFramebuffersPtr)a_GetProcAddress("glGenFramebuffers");
    a_glDeleteTextures = (glDeleteTexturesPtr)a_GetProcAddress("glDeleteTextures");
    a_glDeleteRenderbuffers = (glDeleteRenderbuffersPtr)a_GetProcAddress("glDeleteRenderbuffers");
    a_glDeleteFramebuffers = (glDeleteFramebuffersPtr)a_GetProcAddress("glDeleteFramebuffers");
    a_glFinish = (glFinishPtr)a_GetProcAddress("glFinish");

    a_glRenderbufferStorageMultisample = (glRenderbufferStorageMultisamplePtr)a_GetProcAddress("glRenderbufferStorageMultisample");
    a_glBlitFramebuffer = (glBlitFramebufferPtr)a_GetProcAddress("glBlitFramebuffer");
    a_glGetIntegerv = (glGetIntegervPtr)a_GetProcAddress("glGetIntegerv");

    #if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
    wglChoosePixelFormatARB = (wglChoosePixelFormatARBPtr)a_GetProcAddress("wglChoosePixelFormatARB");
    wglCreateContextAttribsARB = (wglCreateContextAttribsARBPtr)a_GetProcAddress("wglCreateContextAttribsARB");
    wglDXOpenDeviceNV = (wglDXOpenDeviceNVPtr)a_GetProcAddress("wglDXOpenDeviceNV");
    wglDXCloseDeviceNV = (wglDXCloseDeviceNVPtr)a_GetProcAddress("wglDXCloseDeviceNV");
    wglDXRegisterObjectNV = (wglDXRegisterObjectNVPtr)a_GetProcAddress("wglDXRegisterObjectNV");
    wglDXSetResourceShareHandleNV = (wglDXSetResourceShareHandleNVPtr)a_GetProcAddress("wglDXSetResourceShareHandleNV");
    wglDXUnregisterObjectNV = (wglDXUnregisterObjectNVPtr)a_GetProcAddress("wglDXUnregisterObjectNV");
    wglDXLockObjectsNV = (wglDXLockObjectsNVPtr)a_GetProcAddress("wglDXLockObjectsNV");
    wglDXUnlockObjectsNV = (wglDXUnlockObjectsNVPtr)a_GetProcAddress("wglDXUnlockObjectsNV");
    #endif
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glDeleteFramebuffers(JNIEnv* env, jobject, jint fbo) {
    a_glDeleteFramebuffers(1, (GLuint*)&fbo);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glDeleteRenderbuffers(JNIEnv* env, jobject, jint rbo) {
    a_glDeleteRenderbuffers(1, (GLuint*)&rbo);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glDeleteTextures(JNIEnv* env, jobject, jint texture) {
    a_glDeleteTextures(1, (GLuint*)&texture);
}

JNIEXPORT jint JNICALL Java_com_huskerdev_openglfx_GLExecutor_glGenFramebuffers(JNIEnv* env, jobject) {
    GLuint framebuffer = 0;
    a_glGenFramebuffers(1, &framebuffer);
    return framebuffer;
}

JNIEXPORT jint JNICALL Java_com_huskerdev_openglfx_GLExecutor_glGenRenderbuffers(JNIEnv* env, jobject) {
    GLuint renderbuffer;
    a_glGenRenderbuffers(1, &renderbuffer);
    return renderbuffer;
}

JNIEXPORT jint JNICALL Java_com_huskerdev_openglfx_GLExecutor_glGenTextures(JNIEnv* env, jobject) {
    GLuint texture;
    a_glGenTextures(1, &texture);
    return texture;
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glBindFramebuffer(JNIEnv* env, jobject, jint target, jint fbo) {
    a_glBindFramebuffer(target, fbo);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glBindRenderbuffer(JNIEnv* env, jobject, jint target, jint rbo) {
    a_glBindRenderbuffer(target, rbo);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glBindTexture(JNIEnv* env, jobject, jint target, jint texture) {
    a_glBindTexture(target, texture);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glFramebufferTexture2D(JNIEnv* env, jobject, jint target, jint attachment, jint texture, jint texId, jint level) {
    a_glFramebufferTexture2D(target, attachment, texture, texId, level);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glRenderbufferStorage(JNIEnv* env, jobject, jint target, jint internalFormat, jint width, jint height) {
    a_glRenderbufferStorage(target, internalFormat, width, height);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glFramebufferRenderbuffer(JNIEnv* env, jobject, jint target, jint attachment, jint renderbufferTarget, jint renderbuffer) {
    a_glFramebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glReadPixels(JNIEnv* env, jobject, jint x, jint y, jint width, jint height, jint format, jint type, jobject pixels) {
    char* bb = (char*)env->GetDirectBufferAddress(pixels);
    a_glReadPixels(x, y, width, height, format, type, bb);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glTexImage2D(JNIEnv* env, jobject, jint target, jint level, jint internalFormat, jint width, jint height, jint border, jint format, jint type, jobject pixels) {
    char* bb = pixels ? (char*)env->GetDirectBufferAddress(pixels) : NULL;
    a_glTexImage2D(target, level, internalFormat, width, height, border, format, type, bb);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glTexParameteri(JNIEnv* env, jobject, jint target, jint pname, jint param) {
    a_glTexParameteri(target, pname, param);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glViewport(JNIEnv* env, jobject, jint x, jint y, jint w, jint h) {
    a_glViewport(x, y, w, h);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glFinish(JNIEnv* env, jobject) {
    a_glFinish();
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glRenderbufferStorageMultisample(JNIEnv* env, jobject, jint target, jint samples, jint internalformat, jint width, jint height) {
    a_glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_GLExecutor_glBlitFramebuffer(JNIEnv* env, jobject, jint srcX0, jint srcY0, jint srcX1, jint srcY1, jint dstX0, jint dstY0, jint dstX1, jint dstY1, jint mask, jint filter) {
    a_glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
}

JNIEXPORT jint JNICALL Java_com_huskerdev_openglfx_GLExecutor_glGetInteger(JNIEnv* env, jobject, jint pname) {
    GLint data = 0;
    a_glGetIntegerv(pname, &data);
    return (jint)data;
}
}