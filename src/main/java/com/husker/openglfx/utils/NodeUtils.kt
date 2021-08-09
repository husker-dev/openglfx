package com.husker.openglfx.utils

import javafx.scene.Node

class NodeUtils {

    companion object{
        @JvmStatic fun onWindowReady(node: Node, listener: () -> Unit){
            if(node.scene != null){
                if(node.scene.window != null)
                    listener.invoke()
                else
                    node.scene.windowProperty().addListener { _, _, _ -> listener.invoke() }
            }else{
                node.sceneProperty().addListener { _, _, _ ->
                    if(node.scene.window != null)
                        listener.invoke()
                    else
                        node.scene.windowProperty().addListener { _, _, _ -> listener.invoke() }
                }
            }
        }
    }
}