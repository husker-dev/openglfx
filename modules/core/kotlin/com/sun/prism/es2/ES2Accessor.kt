package com.sun.prism.es2

import com.sun.prism.Texture

val Texture.esTextureId: Int
    get() = (this as ES2RTTexture).nativeSourceHandle