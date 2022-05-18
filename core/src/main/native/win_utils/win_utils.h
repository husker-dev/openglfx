#include <d3d9.h>
#include <gl/gl.h>

#include "wgl.h"
#include <jni.h>
#include <iostream>


// Emulate internal JavaFX's code for memory mapping
struct IManagedResource {
    void* virtualTable;

	IManagedResource* pPrev;
	IManagedResource* pNext;
};

struct D3DResource {
	IManagedResource managedResource;
	IDirect3DResource9* pResource;
	IDirect3DSwapChain9* pSwapChain;
	IDirect3DSurface9* pSurface;
	IDirect3DSurface9* pDepthSurface;
	IDirect3DTexture9* pTexture;

	D3DSURFACE_DESC      desc;
};

extern "C" {

    JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_wglDXLockObjectsNV(JNIEnv* env, jobject, jlong funP, jlong handle, jlong textureHandle) {
        PFNWGLDXLOCKOBJECTSNVPROC wglDXLockObjectsNV = (PFNWGLDXLOCKOBJECTSNVPROC)funP;
        HANDLE sharedTextureHandle = (HANDLE)textureHandle;
        
        return wglDXLockObjectsNV((HANDLE)handle, 1, &sharedTextureHandle);
    }

    JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_wglDXUnlockObjectsNV(JNIEnv* env, jobject, jlong funP, jlong handle, jlong textureHandle) {
        PFNWGLDXUNLOCKOBJECTSNVPROC wglDXUnlockObjectsNV = (PFNWGLDXUNLOCKOBJECTSNVPROC)funP;
        HANDLE sharedTextureHandle = (HANDLE)textureHandle;

        return wglDXUnlockObjectsNV((HANDLE)handle, 1, &sharedTextureHandle);
    }

    JNIEXPORT jlongArray JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_createD3DTexture(JNIEnv* env, jobject, jlong _device, jint width, jint height) {
        IDirect3DDevice9Ex* device = (IDirect3DDevice9Ex*)_device;

        // It is impportant to set NULL
        IDirect3DTexture9* texture = NULL;
        HANDLE sharedHandle = NULL;

        HRESULT h;
        if ((h = device->CreateTexture(width, height, 0, D3DUSAGE_DYNAMIC, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &texture, &sharedHandle)) != S_OK)
            std::cout << "Failed to create D3D9 texture" << std::endl;

        jlongArray result = env->NewLongArray(2);
        jlong array[2] = { (jlong)texture, (jlong)sharedHandle };
        env->SetLongArrayRegion(result, 0, 2, array);

        return result;
    }

    JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_replaceD3DTextureInResource(JNIEnv* env, jobject, jlong _resource, jlong newTexture) {
        D3DResource* resource = (D3DResource*)_resource;
        IDirect3DTexture9* texture = (IDirect3DTexture9*)newTexture;

        resource->pTexture->Release();
        resource->pResource->Release();
        resource->pSurface->Release();

        resource->pResource = texture;
        resource->pResource->AddRef();
        resource->pTexture = texture;
        resource->pTexture->GetSurfaceLevel(0, &resource->pSurface);
        if (resource->pSurface != nullptr)
            resource->pSurface->GetDesc(&resource->desc);
    }

    JNIEXPORT jlongArray JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_createGLContext(JNIEnv* env, jobject, jboolean isCore, jlong shareRc, jlong choosePixelPtr, jlong createContextPtr) {
        PFNWGLCHOOSEPIXELFORMATARBPROC wglChoosePixelFormatARB = (PFNWGLCHOOSEPIXELFORMATARBPROC)choosePixelPtr;
        PFNWGLCREATECONTEXTATTRIBSARBPROC wglCreateContextAttribsARB = (PFNWGLCREATECONTEXTATTRIBSARBPROC)createContextPtr;

        WNDCLASS wc = {};
        wc.lpfnWndProc = DefWindowProc;
        wc.hInstance = GetModuleHandle(NULL);
        wc.lpszClassName = L"openglfx";
        RegisterClass(&wc);

        HWND hwnd = CreateWindowEx(
            WS_EX_APPWINDOW,
            L"openglfx", L"",
            WS_CLIPSIBLINGS | WS_CLIPCHILDREN | WS_SYSMENU | WS_MINIMIZEBOX | WS_CAPTION | WS_MAXIMIZEBOX | WS_THICKFRAME,
            0, 0,
            100, 100,
            NULL, NULL,
            GetModuleHandle(NULL),
            NULL);
        HDC dc = GetDC(hwnd);

        // Create basic pixel format
        PIXELFORMATDESCRIPTOR pfd = {};
        pfd.nSize = sizeof(pfd);
        pfd.dwFlags = PFD_DOUBLEBUFFER | PFD_SUPPORT_OPENGL | PFD_DRAW_TO_WINDOW;
        pfd.iPixelType = PFD_TYPE_RGBA;
        pfd.cColorBits = 24;
        pfd.cDepthBits = 16;
        pfd.iLayerType = PFD_MAIN_PLANE;

        // Create extended pixel format
        int pixel_format_arb;
        UINT pixel_formats_count;

        int pixel_attributes[] = {
            WGL_DRAW_TO_WINDOW_ARB, GL_TRUE,
            WGL_SUPPORT_OPENGL_ARB, GL_TRUE,
            WGL_DOUBLE_BUFFER_ARB, GL_TRUE,
            WGL_COLOR_BITS_ARB, 24,
            WGL_DEPTH_BITS_ARB, 16,
            WGL_ACCELERATION_ARB, WGL_FULL_ACCELERATION_ARB,
            WGL_PIXEL_TYPE_ARB, WGL_TYPE_RGBA_ARB,
            0
        };
        if (!wglChoosePixelFormatARB(dc, pixel_attributes, NULL, 1, &pixel_format_arb, &pixel_formats_count))
            std::cout << "Failed to choose supported pixel format (WGL)" << std::endl;
        if (!SetPixelFormat(dc, pixel_format_arb, &pfd))
            std::cout << "Failed to set pixel format (WGL)" << std::endl;

        // Create context
        GLint context_attributes[] = {
            WGL_CONTEXT_PROFILE_MASK_ARB, isCore ? WGL_CONTEXT_CORE_PROFILE_BIT_ARB : WGL_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB,
            0
        };

        HGLRC rc;
        if (!(rc = wglCreateContextAttribsARB(dc, (HGLRC)shareRc, context_attributes)))
            std::cout << "Failed to create context (WGL)" << std::endl;
        wglMakeCurrent(nullptr, nullptr);

        jlongArray result = env->NewLongArray(2);
        jlong array[2] = { (jlong)rc, (jlong)dc };
        env->SetLongArrayRegion(result, 0, 2, array);

        return result;
    }
}