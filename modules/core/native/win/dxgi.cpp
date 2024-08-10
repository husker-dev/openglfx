#include "openglfx-windows.h"

#include <dxgi.h>


extern glCreateMemoryObjectsEXTPtr      glCreateMemoryObjectsEXT;
extern glDeleteMemoryObjectsEXTPtr      glDeleteMemoryObjectsEXT;

extern glImportMemoryWin32HandleEXTPtr  glImportMemoryWin32HandleEXT;
extern glTextureStorageMem2DEXTPtr      glTextureStorageMem2DEXT;


jni_win_dxgi(jint, glCreateMemoryObjectsEXT)(JNIEnv* env, jobject) {
    checkWGLFunctions();
    GLuint res;
    glCreateMemoryObjectsEXT(1, &res);
    return (jint)res;
}

jni_win_dxgi(void, glDeleteMemoryObjectsEXT)(JNIEnv* env, jobject, jint memoryObject) {
    checkWGLFunctions();
    glDeleteMemoryObjectsEXT(1, (const GLuint*)&memoryObject);
}

jni_win_dxgi(void, glImportMemoryWin32HandleEXT)(JNIEnv* env, jobject, jint memory, jlong size, jint handleType, jlong handle) {
    checkWGLFunctions();
    glImportMemoryWin32HandleEXT(memory, size, handleType, (void*)handle);
}

jni_win_dxgi(void, glTextureStorageMem2DEXT)(JNIEnv* env, jobject, jint texture, jint levels, jint internalFormat, jint width, jint height, jint memory, jlong offset) {
    checkWGLFunctions();
    glTextureStorageMem2DEXT(texture, levels, internalFormat, width, height, memory, offset);
}
