#include <jni.h>

#define glfun(returnType, fun) extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_GLExecutor_##fun
#define nvdxfun(returnType, fun) extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_d3d9_NVDXInterop_##fun
#define d3dfun(returnType, fun) extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_d3d9_D3D9Device_##fun
#define iosfun(returnType, fun) extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_iosurface_IOSurface_##fun

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
#define GL_FALSE 0
#define GL_TRUE 1

void* a_GetProcAddress(const char* name);

#if defined(_WIN32) || defined(_WIN64) || defined(__CYGWIN__)
#include <windows.h>
static HMODULE libGL;

typedef void* (*wglGetProcAddressPtr)(const char*);
typedef BOOL (*wglChoosePixelFormatARBPtr)(HDC hdc, const int* piAttribIList, const FLOAT* pfAttribFList, UINT nMaxFormats, int* piFormats, UINT* nNumFormats);
typedef HGLRC (*wglCreateContextAttribsARBPtr)(HDC hDC, HGLRC hShareContext, const int* attribList);
typedef HANDLE (*wglDXOpenDeviceNVPtr)(void* dxDevice);
typedef BOOL (*wglDXCloseDeviceNVPtr)(HANDLE hDevice);
typedef HANDLE (*wglDXRegisterObjectNVPtr)(HANDLE hDevice, void* dxObject, GLuint name, GLenum type, GLenum access);
typedef BOOL (*wglDXSetResourceShareHandleNVPtr)(void* dxObject, HANDLE shareHandle);
typedef BOOL (*wglDXUnregisterObjectNVPtr)(HANDLE hDevice, HANDLE hObject);
typedef BOOL (*wglDXLockObjectsNVPtr)(HANDLE hDevice, GLint count, HANDLE* hObjects);
typedef BOOL (*wglDXUnlockObjectsNVPtr) (HANDLE hDevice, GLint count, HANDLE* hObjects);

static wglGetProcAddressPtr             a_wglGetProcAddress;
extern wglChoosePixelFormatARBPtr       wglChoosePixelFormatARB;
extern wglCreateContextAttribsARBPtr    wglCreateContextAttribsARB;
extern wglDXOpenDeviceNVPtr             wglDXOpenDeviceNV;
extern wglDXCloseDeviceNVPtr            wglDXCloseDeviceNV;
extern wglDXRegisterObjectNVPtr         wglDXRegisterObjectNV;
extern wglDXSetResourceShareHandleNVPtr wglDXSetResourceShareHandleNV;
extern wglDXUnregisterObjectNVPtr       wglDXUnregisterObjectNV;
extern wglDXLockObjectsNVPtr            wglDXLockObjectsNV;
extern wglDXUnlockObjectsNVPtr          wglDXUnlockObjectsNV;

#elif defined(__linux__)
#include <dlfcn.h>
static void* libGL;

typedef void* (* glXGetProcAddressPtr)(const char*);
static glXGetProcAddressPtr a_gladGetProcAddress;

#elif defined(__APPLE__)
#include <dlfcn.h>
static void* libGL;
#endif


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
