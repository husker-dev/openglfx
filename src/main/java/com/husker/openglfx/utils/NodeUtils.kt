package com.husker.openglfx.utils

import javafx.scene.Node
import javafx.scene.Scene

class NodeUtils {

    companion object{
        @JvmStatic fun onWindowReady(node: Node, listener: () -> Unit){
            if(node.scene != null)
                processScene(node.scene, listener)
            else
                node.sceneProperty().addListener { _, _, _ -> processScene(node.scene, listener) }
        }

        private fun processScene(scene: Scene, listener: () -> Unit){
            if(scene.window != null)
                listener.invoke()
            else
                scene.windowProperty().addListener { _, _, _ -> listener.invoke() }
        }
    }
}