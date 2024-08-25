#include "openglfx-windows.h"

#include <d3d9.h>


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


extern "C"{
    typedef jlong (*nGetContextPtr) (JNIEnv *jEnv, jclass, jint adapterOrdinal);
    typedef jlong (*nGetDevicePtr) (JNIEnv *jEnv, jclass, jlong context);

    static nGetContextPtr nGetContext;
    static nGetDevicePtr nGetDevice;
}

jni_win_d3d9(jlong, nGetDeviceFromAdapter)(JNIEnv* env, jclass obj, jint adapterOrdinal) {
    HMODULE module = GetModuleHandleA("prism_d3d.dll");
    nGetContext = (nGetContextPtr)GetProcAddress(module, "Java_com_sun_prism_d3d_D3DResourceFactory_nGetContext");
    nGetDevice = (nGetDevicePtr)GetProcAddress(module, "Java_com_sun_prism_d3d_D3DResourceFactory_nGetDevice");

    return nGetDevice(env, obj, nGetContext(env, obj, adapterOrdinal));
}

jni_win_d3d9(jlong, nCreateDeviceEx)(JNIEnv* env, jobject) {
    IDirect3D9Ex* direct3D = NULL;
    Direct3DCreate9Ex(D3D_SDK_VERSION, &direct3D);

    D3DPRESENT_PARAMETERS pp = {};
    pp.Windowed = TRUE;
    pp.SwapEffect = D3DSWAPEFFECT_DISCARD;
    pp.hDeviceWindow = NULL;
    pp.BackBufferCount = 1;
    pp.BackBufferWidth = 1;
    pp.BackBufferHeight = 1;

    IDirect3DDevice9Ex* device = NULL;
    direct3D->CreateDeviceEx(
           D3DADAPTER_DEFAULT, D3DDEVTYPE_HAL,
           NULL,
           D3DCREATE_HARDWARE_VERTEXPROCESSING | D3DCREATE_MULTITHREADED,
           &pp, NULL, &device);

    return (jlong)device;
}

jni_win_d3d9(jlongArray, nCreateTexture)(JNIEnv* env, jobject, jlong _device, jint width, jint height, jlong _shareHandle) {
    IDirect3DDevice9Ex* device = (IDirect3DDevice9Ex*)_device;

    // It is important to set NULL
    IDirect3DTexture9* texture = NULL;
    HANDLE sharedHandle = (HANDLE)_shareHandle;
    device->CreateTexture(width, height, 1, D3DUSAGE_RENDERTARGET, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &texture, &sharedHandle);

    return createLongArray(env, { (jlong)texture, (jlong)sharedHandle });
}

jni_win_d3d9(void, nReleaseTexture)(JNIEnv* env, jobject, jlong handle) {
    IDirect3DTexture9* texture = (IDirect3DTexture9*)handle;
    texture->Release();
}

jni_win_d3d9(void, replaceD3DTextureInResource)(JNIEnv* env, jobject, jlong _resource, jlong newTexture) {
    D3DResource* resource = (D3DResource*)_resource;
    IDirect3DTexture9* texture = (IDirect3DTexture9*)newTexture;

    resource->pTexture->Release();
    resource->pResource->Release();
    resource->pSurface->Release();

    // From D3DResource in D3DResourceManager.cpp
    resource->pResource = texture;
    resource->pResource->AddRef();
    resource->pTexture = (IDirect3DTexture9*)resource->pResource;
    resource->pTexture->GetSurfaceLevel(0, &resource->pSurface);
    resource->pSurface->GetDesc(&resource->desc);
}