#ifndef OPENGLFX_H
#define OPENGLFX_H

#include <jni.h>
#include <initializer_list>

#define jni_utils(returnType, fun)          extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_GLFXUtils_##fun
#define jni_gl(returnType, fun)             extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_GLExecutor_##fun
#define jni_memoryObjects(returnType, fun)  extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_platforms_MemoryObjects_##fun
#define jni_vkextmemory(returnType, fun)    extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_platforms_VkExtMemory_##fun


typedef unsigned int GLenum;
typedef int GLint;
typedef unsigned int GLuint;
typedef float GLfloat;
typedef int GLsizei;
#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
    typedef long long int GLsizeiptr;
#else
    typedef long GLsizeiptr;
#endif
typedef unsigned char GLubyte;
typedef char GLchar;
typedef unsigned char GLboolean;
typedef unsigned int GLbitfield;

#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
    typedef unsigned long GLuint64;
#endif

#define GL_FALSE 0
#define GL_TRUE 1


#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
#include <windows.h>
static HMODULE libGL;

typedef void* (*wglGetProcAddressPtr)(const char*);
static wglGetProcAddressPtr a_wglGetProcAddress;

#elif defined(__linux__)
#include <dlfcn.h>
static void* libGL;

typedef void* (* glXGetProcAddressPtr)(const char*);
static glXGetProcAddressPtr a_gladGetProcAddress;

#elif defined(__APPLE__)
#include <dlfcn.h>
static void* libGL;
#endif



static void* a_GetProcAddress(const char* name) {
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
        a_gladGetProcAddress = (glXGetProcAddressPtr)dlsym(libGL, "glXGetProcAddressARB");
    }
    void* procAddr = (void*)a_gladGetProcAddress(name);
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

typedef void (*glActiveTexturePtr)(GLenum unit);
typedef void (*glEnablePtr)(GLint cap);
typedef void (*glDisablePtr)(GLint cap);
typedef void (*glDeleteFramebuffersPtr)(GLsizei n, const GLuint* framebuffers);
typedef void (*glDeleteRenderbuffersPtr)(GLsizei n, const GLuint* renderbuffers);
typedef void (*glDeleteTexturesPtr)(GLsizei n, const GLuint* textures);
typedef void (*glGenFramebuffersPtr)(GLsizei n, GLuint* framebuffers);
typedef void (*glGenRenderbuffersPtr)(GLsizei n, GLuint* renderbuffers);
typedef void (*glGenTexturesPtr)(GLsizei n, GLuint* textures);
typedef void (*glBindFramebufferPtr)(GLenum target, GLuint framebuffer);
typedef void (*glBindRenderbufferPtr)(GLenum target, GLuint renderbuffer);
typedef void (*glBindTexturePtr)(GLenum target, GLuint texture);
typedef void (*glFramebufferTexture2DPtr)(GLenum target, GLenum attachment, GLenum textarget, GLuint texture, GLint level);
typedef void (*glRenderbufferStoragePtr)(GLenum target, GLenum internalformat, GLsizei width, GLsizei height);
typedef void (*glFramebufferRenderbufferPtr)(GLenum target, GLenum attachment, GLenum renderbuffertarget, GLuint renderbuffer);
typedef void (*glReadPixelsPtr)(GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, void* pixels);
typedef void (*glTexImage2DPtr)(GLenum target, GLint level, GLint internalformat, GLsizei width, GLsizei height, GLint border, GLenum format, GLenum type, const void* pixels);
typedef void (*glTexParameteriPtr)(GLenum target, GLenum pname, GLint param);
typedef void (*glViewportPtr)(GLint x, GLint y, GLsizei width, GLsizei height);
typedef void (*glFinishPtr)(void);
typedef GLenum (*glGetErrorPtr)(void);
typedef void (*glCopyTexSubImage2DPtr)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint x, GLint y, GLsizei width, GLsizei height);

typedef void (*glRenderbufferStorageMultisamplePtr)(GLenum target, GLsizei samples, GLint internalformat, GLsizei width, GLsizei height);
typedef void (*glBlitFramebufferPtr)(GLint srcX0, GLint srcY0, GLint srcX1, GLint srcY1, GLint dstX0, GLint dstY0, GLint dstX1, GLint dstY1, GLbitfield mask, GLenum filter);
typedef void (*glGetIntegervPtr)(GLenum pname, GLint* data);

typedef GLuint (*glCreateShaderPtr)(GLenum type);
typedef void (*glDeleteShaderPtr)(GLuint shader);
typedef void (*glShaderSourcePtr)(GLuint shader, GLsizei count, const GLchar *const*string, const GLint *length);
typedef void (*glCompileShaderPtr)(GLuint shader);
typedef GLuint (*glCreateProgramPtr)(void);
typedef void (*glAttachShaderPtr)(GLuint program, GLuint shader);
typedef void (*glLinkProgramPtr)(GLuint program);
typedef void (*glUseProgramPtr)(GLuint program);
typedef GLint (*glGetUniformLocationPtr)(GLuint program, const GLchar *name);
typedef GLint (*glGetAttribLocationPtr)(GLuint program, const GLchar *name);
typedef void (*glUniform2fPtr)(GLint location, GLfloat v0, GLfloat v1);
typedef void (*glGetShaderivPtr)(GLuint shader, GLenum pname, GLint *params);
typedef void (*glGetShaderInfoLogPtr)(GLuint shader, GLsizei maxLength, GLsizei *length, GLchar *infoLog);

typedef void (*glGenVertexArraysPtr)(GLsizei n, GLuint *arrays);
typedef void (*glBindVertexArrayPtr)(GLuint array);
typedef void (*glGenBuffersPtr)(GLsizei n, GLuint *buffers);
typedef void (*glBindBufferPtr)(GLenum target, GLuint buffer);
typedef void (*glBufferDataPtr)(GLenum target, GLsizeiptr size, const void *data, GLenum usage);
typedef void (*glVertexAttribPointerPtr)(GLuint index, GLint size, GLenum type, GLboolean normalized, GLsizei stride, const void *pointer);
typedef void (*glEnableVertexAttribArrayPtr)(GLuint index);
typedef void (*glDeleteBuffersPtr)(GLsizei n, const GLuint *buffers);
typedef void (*glDrawArraysPtr)(GLenum mode, GLint first, GLsizei count);


static glActiveTexturePtr               a_glActiveTexture;
static glEnablePtr                      a_glEnable;
static glDisablePtr                     a_glDisable;
static glViewportPtr                    a_glViewport;
static glTexParameteriPtr               a_glTexParameteri;
static glTexImage2DPtr                  a_glTexImage2D;
static glReadPixelsPtr                  a_glReadPixels;
static glFramebufferRenderbufferPtr     a_glFramebufferRenderbuffer;
static glRenderbufferStoragePtr         a_glRenderbufferStorage;
static glFramebufferTexture2DPtr        a_glFramebufferTexture2D;
static glBindTexturePtr                 a_glBindTexture;
static glBindRenderbufferPtr            a_glBindRenderbuffer;
static glBindFramebufferPtr             a_glBindFramebuffer;
static glGenTexturesPtr                 a_glGenTextures;
static glGenRenderbuffersPtr            a_glGenRenderbuffers;
static glGenFramebuffersPtr             a_glGenFramebuffers;
static glDeleteTexturesPtr              a_glDeleteTextures;
static glDeleteRenderbuffersPtr         a_glDeleteRenderbuffers;
static glDeleteFramebuffersPtr          a_glDeleteFramebuffers;
static glFinishPtr                      a_glFinish;
static glGetErrorPtr                    a_glGetError;
static glCopyTexSubImage2DPtr           a_glCopyTexSubImage2D;

static glRenderbufferStorageMultisamplePtr a_glRenderbufferStorageMultisample;
static glBlitFramebufferPtr             a_glBlitFramebuffer;
static glGetIntegervPtr                 a_glGetIntegerv;

static glCreateShaderPtr                a_glCreateShader;
static glDeleteShaderPtr                a_glDeleteShader;
static glShaderSourcePtr                a_glShaderSource;
static glCompileShaderPtr               a_glCompileShader;
static glCreateProgramPtr               a_glCreateProgram;
static glAttachShaderPtr                a_glAttachShader;
static glLinkProgramPtr                 a_glLinkProgram;
static glUseProgramPtr                  a_glUseProgram;
static glGetUniformLocationPtr          a_glGetUniformLocation;
static glGetAttribLocationPtr           a_glGetAttribLocation;
static glUniform2fPtr                   a_glUniform2f;
static glGetShaderivPtr                 a_glGetShaderiv;
static glGetShaderInfoLogPtr            a_glGetShaderInfoLog;

static glGenVertexArraysPtr             a_glGenVertexArrays;
static glBindVertexArrayPtr             a_glBindVertexArray;
static glGenBuffersPtr                  a_glGenBuffers;
static glBindBufferPtr                  a_glBindBuffer;
static glBufferDataPtr                  a_glBufferData;
static glVertexAttribPointerPtr         a_glVertexAttribPointer;
static glEnableVertexAttribArrayPtr     a_glEnableVertexAttribArray;
static glDeleteBuffersPtr               a_glDeleteBuffers;
static glDrawArraysPtr                  a_glDrawArrays;

// JNI utils

static jlongArray createLongArray(JNIEnv* env, int size, jlong* array){
    jlongArray result = env->NewLongArray(size);
    env->SetLongArrayRegion(result, 0, size, array);
    return result;
}

static jlongArray createLongArray(JNIEnv* env, std::initializer_list<jlong> array){
    return createLongArray(env, (int)array.size(), (jlong*)array.begin());
}

#endif