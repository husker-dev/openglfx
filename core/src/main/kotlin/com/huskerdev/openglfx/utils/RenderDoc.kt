package com.huskerdev.openglfx.utils

import com.huskerdev.ojgl.GLContext

class RenderDoc {
    companion object {
        @JvmStatic private external fun nInitRenderDoc(): Boolean
        @JvmStatic private external fun nStartFrameCapture(context: Long)
        @JvmStatic private external fun nEndFrameCapture(context: Long)

        private var isInitialized = false

        init {
            OpenGLFXLibLoader.load()
        }

        private fun checkLoad(){
            if(!isInitialized && nInitRenderDoc())
                isInitialized = true
        }

        fun startFrameCapture(context: GLContext = GLContext.current()) {
            checkLoad()
            nStartFrameCapture(context.handle)
        }

        fun endFrameCapture(context: GLContext = GLContext.current()) {
            checkLoad()
            nEndFrameCapture(context.handle)
        }
    }
}