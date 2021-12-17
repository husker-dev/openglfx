package com.husker.openglfx.utils

import com.sun.javafx.geom.BaseBounds
import com.sun.javafx.geom.PickRay
import com.sun.javafx.geom.transform.BaseTransform
import com.sun.javafx.scene.input.PickResultChooser
import com.sun.javafx.scene.layout.RegionHelper
import com.sun.javafx.sg.prism.NGNode
import javafx.geometry.Bounds
import javafx.scene.Node

class RegionAccessorOverrider {

    companion object{
        inline fun <reified T: Node> overwrite(obj: RegionAccessorObject<T>){
            val regionAccessorField = RegionHelper::class.java.getDeclaredField("regionAccessor")
            regionAccessorField.isAccessible = true
            obj.originalAccessor = regionAccessorField[null] as RegionHelper.RegionAccessor

            regionAccessorField[null] = object : RegionHelper.RegionAccessor {
                override fun doCreatePeer(node: Node): NGNode {
                    return if(node is T) obj.doCreatePeer(node)
                    else obj.originalAccessor.doCreatePeer(node)
                }
                override fun doUpdatePeer(node: Node) {
                    return if(node is T) obj.doUpdatePeer(node)
                    else obj.originalAccessor.doUpdatePeer(node)
                }
                override fun doComputeLayoutBounds(node: Node): Bounds {
                    return if(node is T) obj.doComputeLayoutBounds(node)
                    else obj.originalAccessor.doComputeLayoutBounds(node)
                }
                override fun doComputeGeomBounds(node: Node, bounds: BaseBounds, tx: BaseTransform): BaseBounds {
                    return if(node is T) obj.doComputeGeomBounds(node, bounds, tx)
                    else obj.originalAccessor.doComputeGeomBounds(node, bounds, tx)
                }
                override fun doComputeContains(node: Node, localX: Double, localY: Double): Boolean {
                    return if(node is T) obj.doComputeContains(node, localX, localY)
                    else obj.originalAccessor.doComputeContains(node, localX, localY)
                }
                override fun doNotifyLayoutBoundsChanged(node: Node) {
                    return if(node is T) obj.doNotifyLayoutBoundsChanged(node)
                    else obj.originalAccessor.doNotifyLayoutBoundsChanged(node)
                }
                override fun doPickNodeLocal(node: Node, localPickRay: PickRay, result: PickResultChooser) {
                    return if(node is T) obj.doPickNodeLocal(node, localPickRay, result)
                    else obj.originalAccessor.doPickNodeLocal(node, localPickRay, result)
                }
            }
        }
    }
}

open class RegionAccessorObject<T: Node>{
    lateinit var originalAccessor: RegionHelper.RegionAccessor

    open fun doUpdatePeer(node: T) = originalAccessor.doUpdatePeer(node)
    open fun doCreatePeer(node: T) = originalAccessor.doCreatePeer(node)!!
    open fun doComputeLayoutBounds(node: T) = originalAccessor.doComputeLayoutBounds(node)!!
    open fun doComputeGeomBounds(node: T, bounds: BaseBounds, tx: BaseTransform) = originalAccessor.doComputeGeomBounds(node, bounds, tx)!!
    open fun doComputeContains(node: T, localX: Double, localY: Double) = originalAccessor.doComputeContains(node, localX, localY)
    open fun doNotifyLayoutBoundsChanged(node: T) = originalAccessor.doNotifyLayoutBoundsChanged(node)
    open fun doPickNodeLocal(node: T, localPickRay: PickRay, result: PickResultChooser) = originalAccessor.doPickNodeLocal(node, localPickRay, result)
}