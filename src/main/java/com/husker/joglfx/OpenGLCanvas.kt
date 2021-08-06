package com.husker.joglfx

import com.jogamp.newt.NewtFactory
import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.util.FPSAnimator
import javafx.application.Platform
import javafx.scene.layout.Pane

class OpenGLCanvas(capabilities: GLCapabilities, listener: GLEventListener): Pane() {

    companion object{
        fun createGLWindow(capabilities: GLCapabilities, listener: GLEventListener): GLWindow {
            val screen = NewtFactory.createScreen(NewtFactory.createDisplay(null, false), 0)
            val window = GLWindow.create(screen, capabilities)
            window.addGLEventListener(listener)
            return window
        }
    }

    constructor(listener: GLEventListener): this(GLCapabilities(GLProfile.getMaxFixedFunc(true)), listener)

    val glWindow: GLWindow = createGLWindow(capabilities, listener)
    val canvas = NewtCanvasJFX(glWindow)

    init{
        Thread{
            var oldGLWidth = 0.0
            var oldGLHeight = 0.0
            var oldDpi = 0.0
            while(true){
                Thread.sleep(1)
                if(oldGLWidth != this.width || oldGLHeight != this.height || (scene != null && scene.window != null && oldDpi != scene.window.outputScaleX)){
                    oldGLWidth = this.width
                    oldGLHeight = this.height
                    if(scene != null && scene.window != null)
                        oldDpi = scene.window.outputScaleX
                    updateContent()
                }
            }
        }.start()
        FPSAnimator(glWindow, 1000).start()

        children.add(canvas)
    }

    private fun updateContent(){
        Platform.runLater {
            canvas.width = this.width
            canvas.height = this.height
        }
    }
}