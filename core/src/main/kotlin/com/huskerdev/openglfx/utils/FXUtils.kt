package com.huskerdev.openglfx.utils


import com.sun.prism.GraphicsPipeline
import javafx.scene.Node
import javafx.scene.Scene

class FXUtils {
    companion object {

        val pipelineName: String
            get() = GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]

        fun onWindowReady(node: Node, listener: () -> Unit){
            if(node.scene != null)
                processScene(node.scene, listener)
            else node.sceneProperty().addListener { _, _, _ -> processScene(node.scene, listener) }
        }

        private fun processScene(scene: Scene, listener: () -> Unit){
            if(scene.window != null)
                listener.invoke()
            else scene.windowProperty().addListener { _, _, _ -> listener.invoke() }
        }
    }
}