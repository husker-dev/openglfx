package com.husker.openglfx.utils

import com.sun.javafx.geom.BaseBounds
import com.sun.javafx.geom.PickRay
import com.sun.javafx.geom.transform.BaseTransform
import com.sun.javafx.scene.input.PickResultChooser
import com.sun.javafx.scene.layout.RegionHelper
import com.sun.javafx.sg.prism.NGNode
import javafx.geometry.Bounds
import javafx.scene.Node

class RegionAccessorRewriter {

    companion object{
        inline fun <reified T> rewrite(crossinline createPeer: (T) -> NGNode){
            val regionAccessorField = RegionHelper::class.java.getDeclaredField("regionAccessor")
            regionAccessorField.isAccessible = true
            val originalAccessor = regionAccessorField[null] as RegionHelper.RegionAccessor

            regionAccessorField[null] = object : RegionHelper.RegionAccessor {
                override fun doCreatePeer(node: Node): NGNode {
                    return if(node is T)
                        createPeer.invoke(node)
                    else originalAccessor.doCreatePeer(node)
                }
                // Call original methods
                override fun doUpdatePeer(node: Node) = originalAccessor.doUpdatePeer(node)
                override fun doComputeLayoutBounds(node: Node): Bounds = originalAccessor.doComputeLayoutBounds(node)
                override fun doComputeGeomBounds(node: Node, bounds: BaseBounds, tx: BaseTransform): BaseBounds = originalAccessor.doComputeGeomBounds(node, bounds, tx)
                override fun doComputeContains(node: Node, localX: Double, localY: Double): Boolean = originalAccessor.doComputeContains(node, localX, localY)
                override fun doNotifyLayoutBoundsChanged(node: Node) = originalAccessor.doNotifyLayoutBoundsChanged(node)
                override fun doPickNodeLocal(node: Node, localPickRay: PickRay, result: PickResultChooser) = originalAccessor.doPickNodeLocal(node, localPickRay, result)
            }
        }
    }
}