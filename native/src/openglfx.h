#include <jni.h>

typedef unsigned int GLenum;
typedef int GLint;
typedef unsigned int GLuint;
typedef int GLsizei;
typedef unsigned char GLubyte;
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
#include <GL/gl.h>
#include <GL/glx.h>
static void* libGL;

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
typedef void (*glTexParameteri)(GLenum target, GLenum pname, GLint param);
typedef void (*glViewportPtr)(GLint x, GLint y, GLsizei width, GLsizei height);
typedef void (*glFinishPtr)(void);
typedef void (*glRenderbufferStorageMultisamplePtr)(GLenum target, GLsizei samples, GLint internalformat, GLsizei width, GLsizei height);
typedef void (*glBlitFramebufferPtr)(GLint srcX0, GLint srcY0, GLint srcX1, GLint srcY1, GLint dstX0, GLint dstY0, GLint dstX1, GLint dstY1, GLbitfield mask, GLenum filter);
typedef void (*glGetIntegervPtr)(GLenum pname, GLint* data);

static glViewportPtr                    a_glViewport;
static glTexParameteri                  a_glTexParameteri;
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