#include <d3d9.h>
#include <gl/gl.h>

#include <jni.h>
#include <iostream>


// wglDXLockObjectsNV and wglDXUnlockObjectsNV from wgl.h
typedef BOOL(WINAPI* PFNWGLDXLOCKOBJECTSNVPROC) (HANDLE hDevice, GLint count, HANDLE* hObjects);
typedef BOOL(WINAPI* PFNWGLDXUNLOCKOBJECTSNVPROC) (HANDLE hDevice, GLint count, HANDLE* hObjects);

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

    JNIEXPORT jlong JNICALL Java_com_huskerdev_openglfx_utils_WinUtils_createDummyGLWindow(JNIEnv* env, jobject) {
        PIXELFORMATDESCRIPTOR pfd = {};
        pfd.nSize = sizeof(pfd);

        WNDCLASS wc = {};
        wc.lpfnWndProc = DefWindowProc;
        wc.hInstance = GetModuleHandle(NULL);
        wc.lpszClassName = L"openglfx";
        RegisterClass(&wc);

        HWND hwnd = CreateWindow(
            L"openglfx", L"",
            WS_OVERLAPPEDWINDOW,
            0, 0,
            100, 100,
            NULL, NULL,
            GetModuleHandle(NULL),
            NULL);

        HDC dc = GetDC(hwnd);

        int pixel_format = ChoosePixelFormat(dc, &pfd);
        SetPixelFormat(dc, pixel_format, &pfd);

        return (jlong)dc;
    }
}