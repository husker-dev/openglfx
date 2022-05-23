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

	D3DSURFACE_DESC desc;
};

HDC dc = 0;

PFNWGLCHOOSEPIXELFORMATARBPROC          wglChoosePixelFormatARB;
PFNWGLCREATECONTEXTATTRIBSARBPROC       wglCreateContextAttribsARB;

PFNWGLDXOPENDEVICENVPROC                wglDXOpenDeviceNV = 0;
PFNWGLDXREGISTEROBJECTNVPROC            wglDXRegisterObjectNV;
PFNWGLDXSETRESOURCESHAREHANDLENVPROC    wglDXSetResourceShareHandleNV;
PFNWGLDXUNREGISTEROBJECTNVPROC          wglDXUnregisterObjectNV;
PFNWGLDXLOCKOBJECTSNVPROC               wglDXLockObjectsNV;
PFNWGLDXUNLOCKOBJECTSNVPROC             wglDXUnlockObjectsNV;

extern "C" {

    jlongArray createLongArray(JNIEnv* env, int size, jlong* elements) {
        jlongArray result = env->NewLongArray(size);
        env->SetLongArrayRegion(result, 0, size, elements);
        return result;
    }

    void checkBasicFunctions() {
        if (dc == 0) {
            HDC oldDC = wglGetCurrentDC();
            HGLRC oldRC = wglGetCurrentContext();

            PIXELFORMATDESCRIPTOR pfd = {};
            pfd.nSize = sizeof(pfd);

            WNDCLASS wc = {};
            wc.lpfnWndProc = DefWindowProc;
            wc.hInstance = GetModuleHandle(NULL);
            wc.lpszClassName = L"openglfx";
            RegisterClass(&wc);

            // Create dummy window to initialize function
            {
                HWND hwnd = CreateWindow(
                    L"openglfx", L"",
                    WS_OVERLAPPEDWINDOW,
                    0, 0,
                    100, 100,
                    NULL, NULL,
                    GetModuleHandle(NULL),
                    NULL);
                HDC dc = GetDC(hwnd);

                int pixel_format = 0;
                if (!(pixel_format = ChoosePixelFormat(dc, &pfd)))
                    std::cout << "Failed to choose pixel format" << std::endl;
                if (!SetPixelFormat(dc, pixel_format, &pfd))
                    std::cout << "Failed to set pixel format" << std::endl;

                HGLRC rc = wglCreateContext(dc);
                wglMakeCurrent(dc, rc);

                // Load functions
                wglChoosePixelFormatARB = (PFNWGLCHOOSEPIXELFORMATARBPROC)wglGetProcAddress("wglChoosePixelFormatARB");
                wglCreateContextAttribsARB = (PFNWGLCREATECONTEXTATTRIBSARBPROC)wglGetProcAddress("wglCreateContextAttribsARB");

                wglDXOpenDeviceNV = (PFNWGLDXOPENDEVICENVPROC)wglGetProcAddress("wglDXOpenDeviceNV");
                wglDXRegisterObjectNV = (PFNWGLDXREGISTEROBJECTNVPROC)wglGetProcAddress("wglDXRegisterObjectNV");
                wglDXSetResourceShareHandleNV = (PFNWGLDXSETRESOURCESHAREHANDLENVPROC)wglGetProcAddress("wglDXSetResourceShareHandleNV");
                wglDXUnregisterObjectNV = (PFNWGLDXUNREGISTEROBJECTNVPROC)wglGetProcAddress("wglDXUnregisterObjectNV");
                wglDXLockObjectsNV = (PFNWGLDXLOCKOBJECTSNVPROC)wglGetProcAddress("wglDXLockObjectsNV");
                wglDXUnlockObjectsNV = (PFNWGLDXUNLOCKOBJECTSNVPROC)wglGetProcAddress("wglDXUnlockObjectsNV");

                // Destroy dummy context
                wglMakeCurrent(oldDC, oldRC);
                wglDeleteContext(rc);
                ReleaseDC(hwnd, dc);
                DestroyWindow(hwnd);
            }
            
            // Create window with ARB pixel attributes
            HWND hwnd = CreateWindow(
                L"openglfx", L"",
                WS_OVERLAPPEDWINDOW,
                0, 0,
                100, 100,
                NULL, NULL,
                GetModuleHandle(NULL),
                NULL);
            dc = GetDC(hwnd);

            int pixel_format_arb;
            UINT pixel_formats_count;

            int pixel_attributes[] = {
                WGL_DRAW_TO_WINDOW_ARB, GL_TRUE,
                WGL_SUPPORT_OPENGL_ARB, GL_TRUE,
                0
            };
            if (!wglChoosePixelFormatARB(dc, pixel_attributes, NULL, 1, &pixel_format_arb, &pixel_formats_count))
                std::cout << "Failed to choose supported pixel format (WGL)" << std::endl;
            if (!SetPixelFormat(dc, pixel_format_arb, &pfd))
                std::cout << "Failed to set pixel format (WGL)" << std::endl;
        }
    }

    JNIEXPORT jlongArray JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_getCurrentContext(JNIEnv* env, jobject) {
        checkBasicFunctions();
        
        jlong array[2] = { (jlong)wglGetCurrentContext(), (jlong)wglGetCurrentDC() };
        return createLongArray(env, 2, array);
    }

    JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_setCurrentContext(JNIEnv* env, jobject, jlong dc, jlong rc) {
        checkBasicFunctions();
        return wglMakeCurrent((HDC)dc, (HGLRC)rc);
    }

    JNIEXPORT jlongArray JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_createContext(JNIEnv* env, jobject, jboolean isCore, jlong shareRc) {
        checkBasicFunctions();

        GLint context_attributes[] = {
            WGL_CONTEXT_PROFILE_MASK_ARB, isCore ? WGL_CONTEXT_CORE_PROFILE_BIT_ARB : WGL_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB,
            0
        };

        HGLRC rc;
        if (!(rc = wglCreateContextAttribsARB(dc, (HGLRC)shareRc, context_attributes)))
            std::cout << "Failed to create context (WGL)" << std::endl;

        jlong array[2] = { (jlong)rc, (jlong)dc };
        return createLongArray(env, 2, array);
    }

    JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_hasDXInterop(JNIEnv* env, jobject) {
        checkBasicFunctions();
        return wglDXOpenDeviceNV != 0;
    }

    JNIEXPORT jlong JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_wglDXOpenDeviceNV(JNIEnv* env, jobject, jlong dxDevice) {
        checkBasicFunctions();
        return (jlong)wglDXOpenDeviceNV((void*)dxDevice);
    }

    JNIEXPORT jlong JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_wglDXRegisterObjectNV(JNIEnv* env, jobject, jlong device, jlong dxResource, jint name, jint type, jint access) {
        checkBasicFunctions();
        return (jlong)wglDXRegisterObjectNV((HANDLE)device, (void*)dxResource, name, type, access);
    }

    JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_wglDXSetResourceShareHandleNV(JNIEnv* env, jobject, jlong dxResource, jlong shareHandle) {
        checkBasicFunctions();
        return (jboolean)wglDXSetResourceShareHandleNV((void*)dxResource, (HANDLE)shareHandle);
    }

    JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_wglDXUnregisterObjectNV(JNIEnv* env, jobject, jlong device, jlong object) {
        checkBasicFunctions();
        return (jboolean)wglDXUnregisterObjectNV((HANDLE)device, (HANDLE)object);
    }

    JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_wglDXLockObjectsNV(JNIEnv* env, jobject, jlong handle, jlong textureHandle) {
        checkBasicFunctions();
        return wglDXLockObjectsNV((HANDLE)handle, 1, (HANDLE*)&textureHandle);
    }

    JNIEXPORT jboolean JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_wglDXUnlockObjectsNV(JNIEnv* env, jobject, jlong handle, jlong textureHandle) {
        checkBasicFunctions();
        return wglDXUnlockObjectsNV((HANDLE)handle, 1, (HANDLE*)&textureHandle);
    }

    /*  ===========
            D3D
        ===========
    */
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
}