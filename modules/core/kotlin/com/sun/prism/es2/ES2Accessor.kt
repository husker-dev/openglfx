package com.sun.prism.es2

import com.sun.prism.Texture

val Texture.glTextureId: Int
    get() = (this as ES2Texture<*>).nativeSourceHandle