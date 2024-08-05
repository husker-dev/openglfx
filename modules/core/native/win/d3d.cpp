#include <openglfx.h>

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

jlongArray createLongArray(JNIEnv* env, int size, jlong* elements) {
    jlongArray result = env->NewLongArray(size);
    env->SetLongArrayRegion(result, 0, size, elements);
    return result;
}

d3dfun(jlongArray, createD3DTexture)(JNIEnv* env, jobject, jlong _device, jint width, jint height) {
    IDirect3DDevice9Ex* device = (IDirect3DDevice9Ex*)_device;

    // It is important to set NULL
    IDirect3DTexture9* texture = NULL;
    HANDLE sharedHandle = NULL;
    device->CreateTexture(width, height, 0, D3DUSAGE_DYNAMIC, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &texture, &sharedHandle);

    jlong array[] = { (jlong)texture, (jlong)sharedHandle };
    return createLongArray(env, 2, array);
}

d3dfun(void, replaceD3DTextureInResource)(JNIEnv* env, jobject, jlong _resource, jlong newTexture) {
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