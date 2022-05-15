#include <d3d9.h>
#include <jni.h>
#include <iostream>

#include <gl/gl.h>

#include "wgl.h"

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

    JNIEXPORT jlong JNICALL Java_com_huskerdev_openglfx_utils_d3d9_D3D9Device_createDevice(JNIEnv* env, jobject) {
        IDirect3D9Ex* d3d9;
        IDirect3DDevice9Ex* d3d9Device;
        Direct3DCreate9Ex(D3D_SDK_VERSION, &d3d9);


        D3DPRESENT_PARAMETERS d3dpp;
        ZeroMemory(&d3dpp, sizeof(d3dpp));
        d3dpp.Windowed = true;
        d3dpp.SwapEffect = D3DSWAPEFFECT_DISCARD;
        d3dpp.hDeviceWindow = NULL;
        d3dpp.BackBufferCount = 1;
        d3dpp.BackBufferWidth = 100;
        d3dpp.BackBufferHeight = 100;

        HRESULT hresult = d3d9->CreateDeviceEx(
            D3DADAPTER_DEFAULT,
            D3DDEVTYPE_HAL,
            NULL,
            D3DCREATE_SOFTWARE_VERTEXPROCESSING,
            &d3dpp,
            NULL,
            &d3d9Device);
        return (jlong)d3d9Device;
    }

	JNIEXPORT jlongArray JNICALL Java_com_huskerdev_openglfx_utils_d3d9_D3D9Device_createTexture(JNIEnv* env, jobject, jlong _device, jint width, jint height) {
		IDirect3DDevice9Ex* device = (IDirect3DDevice9Ex*)_device;

		IDirect3DTexture9* texture = 0;
		HANDLE sharedHandle = 0;

		HRESULT h;
		if ((h = device->CreateTexture(width, height, 0, D3DUSAGE_DYNAMIC, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &texture, &sharedHandle)) != S_OK)
			std::cout << "Error while creation D3D9 render target" << std::endl;

		jlongArray result = env->NewLongArray(2);
		jlong array[2] = {(jlong)texture, (jlong)sharedHandle};
		env->SetLongArrayRegion(result, 0, 2, array);

		return result;
	}

    JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_utils_d3d9_D3D9Texture_releaseTexture(JNIEnv* env, jobject, jlong handle) {
        IDirect3DTexture9* texture = (IDirect3DTexture9*)handle;
        texture->Release();
    }

    JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_utils_d3d9_D3D9Utils_wglDXLockObjectsNV(JNIEnv* env, jobject, jlong funP, jlong handle, jlong textureHandle) {
        PFNWGLDXLOCKOBJECTSNVPROC wglDXLockObjectsNV = (PFNWGLDXLOCKOBJECTSNVPROC)funP;
        HANDLE sharedTextureHandle = (HANDLE)textureHandle;
        
        wglDXLockObjectsNV((HANDLE)handle, 1, &sharedTextureHandle);
    }

    JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_utils_d3d9_D3D9Utils_wglDXUnlockObjectsNV(JNIEnv* env, jobject, jlong funP, jlong handle, jlong textureHandle) {
        PFNWGLDXUNLOCKOBJECTSNVPROC wglDXUnlockObjectsNV = (PFNWGLDXUNLOCKOBJECTSNVPROC)funP;
        HANDLE sharedTextureHandle = (HANDLE)textureHandle;

        wglDXUnlockObjectsNV((HANDLE)handle, 1, &sharedTextureHandle);
    }

    JNIEXPORT void JNICALL Java_com_huskerdev_openglfx_utils_d3d9_D3D9Utils_replaceTextureInResource(JNIEnv* env, jobject, jlong _resource, jlong newTexture) {
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