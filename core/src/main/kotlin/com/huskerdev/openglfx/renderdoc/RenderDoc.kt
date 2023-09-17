package com.huskerdev.openglfx.renderdoc

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.utils.OGLFXLibLoader

class RenderDoc {
    companion object {
        @JvmStatic private external fun nInitRenderDoc(): Boolean
        @JvmStatic private external fun nStartFrameCapture(context: Long)
        @JvmStatic private external fun nEndFrameCapture(context: Long)

        @JvmStatic var enabled = false
        private var isInitialized = false

        init {
            OGLFXLibLoader.load()
        }

        fun loadLibrary(): Boolean{
            if(isInitialized) return true
            isInitialized = nInitRenderDoc()
            return isInitialized
        }

        fun startFrameCapture(context: GLContext = GLContext.current()) {
            if(enabled && loadLibrary())
                nStartFrameCapture(context.handle)
        }

        fun endFrameCapture(context: GLContext = GLContext.current()) {
            if(enabled && loadLibrary())
                nEndFrameCapture(context.handle)
        }
    }
}