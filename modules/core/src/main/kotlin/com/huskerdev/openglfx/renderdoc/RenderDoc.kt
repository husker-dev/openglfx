package com.huskerdev.openglfx.renderdoc

import com.huskerdev.ojgl.GLContext
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.internal.canvas.NVDXInteropCanvasImpl
import com.huskerdev.openglfx.internal.canvas.async.AsyncNVDXInteropCanvasImpl
import com.huskerdev.openglfx.internal.NGGLCanvas
import com.huskerdev.openglfx.internal.GLFXUtils
import com.sun.javafx.scene.layout.RegionHelper
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

class RenderDoc {
    companion object {
        @JvmStatic private external fun nInitRenderDoc(): Boolean
        @JvmStatic private external fun nStartFrameCapture(context: Long)
        @JvmStatic private external fun nEndFrameCapture(context: Long)

        private var isInitialized = false

        init {
            GLFXUtils.loadLibrary()
        }

        private fun loadLibrary(): Boolean{
            if(isInitialized) return true
            isInitialized = nInitRenderDoc()
            return isInitialized
        }

        @JvmStatic
        fun startFrameCapture(context: GLContext = GLContext.current()) {
            if(loadLibrary())
                nStartFrameCapture(context.handle)
        }

        @JvmStatic
        fun endFrameCapture(context: GLContext = GLContext.current()) {
            if(loadLibrary())
                nEndFrameCapture(context.handle)
        }

        @JvmStatic
        fun bind(canvas: GLCanvas, keyCode: KeyCode = KeyCode.F12){
            if(canvas is NVDXInteropCanvasImpl || canvas is AsyncNVDXInteropCanvasImpl)
                println("""
                    WARNING: RenderDoc doesn't support WGL_NV_DX_interop. 
                             Please, use 'OpenGLCanvas.create(..., interopType = GLInteropType.Blit)'.
                             More at: https://github.com/husker-dev/openglfx/issues/39
                """.trimIndent())

            var captureNextFrame = false
            val peer = RegionHelper.getPeer<NGGLCanvas>(canvas)
            peer.addPreRenderListener {
                if(captureNextFrame)
                    startFrameCapture()
            }
            peer.addPostRenderListener {
                if(captureNextFrame) {
                    endFrameCapture()
                    captureNextFrame = false
                }
            }
            peer.addSceneConnectedListener {
                canvas.scene.setOnKeyReleased { event: KeyEvent ->
                    if(event.code == keyCode){
                        captureNextFrame = true
                        canvas.repaint()
                    }
                }
            }
        }
    }
}