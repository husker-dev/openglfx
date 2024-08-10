#ifndef OPENGLFX_WINDOWS_H
#define OPENGLFX_WINDOWS_H

#include <openglfx.h>


#define jni_win(returnType, fun)       extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_platforms_win_WGLFunctions_##fun
#define jni_win_d3d9(returnType, fun)  extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_platforms_win_D3D9_##fun
#define jni_win_wgldx(returnType, fun)  extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_platforms_win_WGLDX_##fun
#define jni_win_dxgi(returnType, fun)  extern "C" JNIEXPORT returnType JNICALL Java_com_huskerdev_openglfx_internal_platforms_win_DXGI_##fun


typedef HGLRC (*wglCreateContextPtr)(HDC);
typedef HGLRC (*wglGetCurrentContextPtr)();
typedef BOOL (*wglDeleteContextPtr)(HGLRC);
typedef BOOL (*wglMakeCurrentPtr)(HDC, HGLRC);

typedef HANDLE (*wglDXOpenDeviceNVPtr)(void* dxDevice);
typedef BOOL (*wglDXCloseDeviceNVPtr)(HANDLE hDevice);
typedef HANDLE (*wglDXRegisterObjectNVPtr)(HANDLE hDevice, void* dxObject, GLuint name, GLenum type, GLenum access);
typedef BOOL (*wglDXSetResourceShareHandleNVPtr)(void* dxObject, HANDLE shareHandle);
typedef BOOL (*wglDXUnregisterObjectNVPtr)(HANDLE hDevice, HANDLE hObject);
typedef BOOL (*wglDXLockObjectsNVPtr)(HANDLE hDevice, GLint count, HANDLE* hObjects);
typedef BOOL (*wglDXUnlockObjectsNVPtr)(HANDLE hDevice, GLint count, HANDLE* hObjects);

typedef void (*glCreateMemoryObjectsEXTPtr)(GLsizei n, GLuint* memoryObjects);
typedef void (*glDeleteMemoryObjectsEXTPtr)(GLsizei n, const GLuint* memoryObjects);
typedef void (*glImportMemoryWin32HandleEXTPtr)(GLuint memory, GLuint64 size, GLenum handleType, void* handle);
typedef void (*glTextureStorageMem2DEXTPtr)(GLuint texture, GLsizei levels, GLenum internalFormat, GLsizei width, GLsizei height, GLuint memory, GLuint64 offset);


static wglDXOpenDeviceNVPtr             wglDXOpenDeviceNV;
static wglDXCloseDeviceNVPtr            wglDXCloseDeviceNV;
static wglDXRegisterObjectNVPtr         wglDXRegisterObjectNV;
static wglDXSetResourceShareHandleNVPtr wglDXSetResourceShareHandleNV;
static wglDXUnregisterObjectNVPtr       wglDXUnregisterObjectNV;
static wglDXLockObjectsNVPtr            wglDXLockObjectsNV;
static wglDXUnlockObjectsNVPtr          wglDXUnlockObjectsNV;

static glCreateMemoryObjectsEXTPtr      glCreateMemoryObjectsEXT;
static glDeleteMemoryObjectsEXTPtr      glDeleteMemoryObjectsEXT;
static glImportMemoryWin32HandleEXTPtr  glImportMemoryWin32HandleEXT;
static glTextureStorageMem2DEXTPtr      glTextureStorageMem2DEXT;

static bool functionsLoaded = false;

static void loadWGLFunctions(){
    wglDXOpenDeviceNV = (wglDXOpenDeviceNVPtr)a_GetProcAddress("wglDXOpenDeviceNV");
    wglDXCloseDeviceNV = (wglDXCloseDeviceNVPtr)a_GetProcAddress("wglDXCloseDeviceNV");
    wglDXRegisterObjectNV = (wglDXRegisterObjectNVPtr)a_GetProcAddress("wglDXRegisterObjectNV");
    wglDXSetResourceShareHandleNV = (wglDXSetResourceShareHandleNVPtr)a_GetProcAddress("wglDXSetResourceShareHandleNV");
    wglDXUnregisterObjectNV = (wglDXUnregisterObjectNVPtr)a_GetProcAddress("wglDXUnregisterObjectNV");
    wglDXLockObjectsNV = (wglDXLockObjectsNVPtr)a_GetProcAddress("wglDXLockObjectsNV");
    wglDXUnlockObjectsNV = (wglDXUnlockObjectsNVPtr)a_GetProcAddress("wglDXUnlockObjectsNV");

    glCreateMemoryObjectsEXT = (glCreateMemoryObjectsEXTPtr)a_GetProcAddress("glCreateMemoryObjectsEXT");
    glDeleteMemoryObjectsEXT = (glDeleteMemoryObjectsEXTPtr)a_GetProcAddress("glDeleteMemoryObjectsEXT");
    glImportMemoryWin32HandleEXT = (glImportMemoryWin32HandleEXTPtr)a_GetProcAddress("glImportMemoryWin32HandleEXT");
    glTextureStorageMem2DEXT = (glTextureStorageMem2DEXTPtr)a_GetProcAddress("glTextureStorageMem2DEXT");
}

static void checkWGLFunctions(){
    if(functionsLoaded)
        return;
    functionsLoaded = true;

    wglCreateContextPtr _wglCreateContext = (wglCreateContextPtr)a_GetProcAddress("wglCreateContext");
    wglGetCurrentContextPtr _wglGetCurrentContext = (wglGetCurrentContextPtr)a_GetProcAddress("wglGetCurrentContext");
    wglDeleteContextPtr _wglDeleteContext = (wglDeleteContextPtr)a_GetProcAddress("wglDeleteContext");
    wglMakeCurrentPtr _wglMakeCurrent = (wglMakeCurrentPtr)a_GetProcAddress("wglMakeCurrent");

    if(_wglGetCurrentContext() != NULL){
        loadWGLFunctions();
        return;
    }

    WNDCLASS wc = {};
    wc.lpfnWndProc = DefWindowProc;
    wc.hInstance = GetModuleHandle(NULL);
    wc.lpszClassName = "nvdx-tmp";
    RegisterClass(&wc);

    PIXELFORMATDESCRIPTOR pfd = {};
    pfd.nVersion = 1;
    pfd.iPixelType = PFD_TYPE_RGBA;
    pfd.cColorBits = 24;
    pfd.cDepthBits = 32;
    pfd.nSize = sizeof(pfd);

    HWND hwnd = CreateWindow(
            "nvdx-tmp", "",
            WS_OVERLAPPEDWINDOW,
            0, 0, 100, 100,
            NULL, NULL, GetModuleHandle(NULL), NULL);
    HDC dc = GetDC(hwnd);

    int pixel_format = ChoosePixelFormat(dc, &pfd);
    SetPixelFormat(dc, pixel_format, &pfd);

    HGLRC rc = _wglCreateContext(dc);
    _wglMakeCurrent(dc, rc);

    loadWGLFunctions();

    _wglDeleteContext(rc);
    ReleaseDC(hwnd, dc);
    DestroyWindow(hwnd);
}

#endif