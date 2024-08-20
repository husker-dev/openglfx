#include "openglfx.h"


typedef void (*glCreateMemoryObjectsEXTPtr)(GLsizei n, GLuint* memoryObjects);
typedef void (*glDeleteMemoryObjectsEXTPtr)(GLsizei n, const GLuint* memoryObjects);
typedef void (*glImportMemoryWin32HandleEXTPtr)(GLuint memory, unsigned long size, GLenum handleType, void* handle);
typedef void (*glTextureStorageMem2DEXTPtr)(GLuint texture, GLsizei levels, GLenum internalFormat, GLsizei width, GLsizei height, GLuint memory, unsigned long offset);
typedef void (*glImportMemoryFdEXTPtr)(GLuint memory, unsigned long size, GLenum handleType, GLint fd);


static glCreateMemoryObjectsEXTPtr      glCreateMemoryObjectsEXT;
static glDeleteMemoryObjectsEXTPtr      glDeleteMemoryObjectsEXT;
static glTextureStorageMem2DEXTPtr      glTextureStorageMem2DEXT;
static glImportMemoryWin32HandleEXTPtr  glImportMemoryWin32HandleEXT;
static glImportMemoryFdEXTPtr           glImportMemoryFdEXT;


jni_memoryObjects(void, nLoadFunctions)(JNIEnv* env, jobject) {
    glCreateMemoryObjectsEXT = (glCreateMemoryObjectsEXTPtr)a_GetProcAddress("glCreateMemoryObjectsEXT");
    glDeleteMemoryObjectsEXT = (glDeleteMemoryObjectsEXTPtr)a_GetProcAddress("glDeleteMemoryObjectsEXT");
    glTextureStorageMem2DEXT = (glTextureStorageMem2DEXTPtr)a_GetProcAddress("glTextureStorageMem2DEXT");
    glImportMemoryWin32HandleEXT = (glImportMemoryWin32HandleEXTPtr)a_GetProcAddress("glImportMemoryWin32HandleEXT");
    glImportMemoryFdEXT = (glImportMemoryFdEXTPtr)a_GetProcAddress("glImportMemoryFdEXT");
}

jni_memoryObjects(jint, glCreateMemoryObjectsEXT)(JNIEnv* env, jobject) {
    GLuint res;
    glCreateMemoryObjectsEXT(1, &res);
    return (jint)res;
}

jni_memoryObjects(void, glDeleteMemoryObjectsEXT)(JNIEnv* env, jobject, jint memoryObject) {
    glDeleteMemoryObjectsEXT(1, (const GLuint*)&memoryObject);
}

jni_memoryObjects(void, glImportMemoryWin32HandleEXT)(JNIEnv* env, jobject, jint memory, jlong size, jint handleType, jlong handle) {
    glImportMemoryWin32HandleEXT(memory, size, handleType, (void*)handle);
}

jni_memoryObjects(void, glTextureStorageMem2DEXT)(JNIEnv* env, jobject, jint texture, jint levels, jint internalFormat, jint width, jint height, jint memory, jlong offset) {
    glTextureStorageMem2DEXT(texture, levels, internalFormat, width, height, memory, offset);
}

jni_memoryObjects(void, glImportMemoryFdEXT)(JNIEnv* env, jobject, jint memory, jlong size, jint handleType, jint fd) {
    glImportMemoryFdEXT(memory, size, handleType, fd);
}
