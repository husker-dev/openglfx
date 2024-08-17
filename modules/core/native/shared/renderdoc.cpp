#include "renderdoc_app.h"
#include <jni.h>

#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
#include <windows.h>
#elif defined(__linux__) || defined(__APPLE__)
#include <dlfcn.h>
#endif

RENDERDOC_API_1_1_2 *rdoc_api = NULL;

extern "C" {

JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_renderdoc_RenderDoc_nInitRenderDoc(JNIEnv* env, jobject) {
    #if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
    if(HMODULE mod = GetModuleHandleA("renderdoc.dll")) {
        auto GetAPI = (pRENDERDOC_GetAPI)GetProcAddress(mod, "RENDERDOC_GetAPI");
        return (jboolean)GetAPI(eRENDERDOC_API_Version_1_6_0, (void **)&rdoc_api);
    }
    #elif defined(__linux__) || defined(__APPLE__)
    if(void *mod = dlopen("librenderdoc.so", RTLD_NOW | RTLD_NOLOAD)) {
        auto GetAPI = (pRENDERDOC_GetAPI)dlsym(mod, "RENDERDOC_GetAPI");
        return (jboolean)GetAPI(eRENDERDOC_API_Version_1_6_0, (void **)&rdoc_api);
    }
    #endif
    return false;
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_renderdoc_RenderDoc_nStartFrameCapture(JNIEnv* env, jobject, jlong context) {
    if(rdoc_api) rdoc_api->StartFrameCapture((RENDERDOC_DevicePointer)context, NULL);
}

JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_renderdoc_RenderDoc_nEndFrameCapture(JNIEnv* env, jobject, jlong context) {
    if(rdoc_api) rdoc_api->EndFrameCapture((RENDERDOC_DevicePointer)context, NULL);
}
}