package com.sun.prism.d3d

import com.sun.prism.Texture

val Texture.d3dTextureResource: Long
    get() = (this as D3DTexture).nativeSourceHandle