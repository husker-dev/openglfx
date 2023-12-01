package com.huskerdev.openglfx.internal

import kotlin.math.max

internal data class Size(
    var width: Int = Integer.MIN_VALUE,
    var height: Int = Integer.MIN_VALUE,
    val minWidth: Int = Integer.MIN_VALUE,
    val minHeight: Int = Integer.MIN_VALUE
) {

    inline fun executeOnDifferenceWith(newWidth: Int, newHeight: Int, consumer: (sizeWidth: Int, sizeHeight: Int) -> Unit){
        if(width != newWidth || height != newHeight){
            width = max(newWidth, minWidth)
            height = max(newHeight, minHeight)
            consumer(width, height)
        }
    }

    inline fun executeOnDifferenceWith(size: Size, consumer: (sizeWidth: Int, sizeHeight: Int) -> Unit) =
        this.executeOnDifferenceWith(size.width, size.height, consumer)

    fun executeOnDifferenceWith(size: Size, vararg consumers: (sizeWidth: Int, sizeHeight: Int) -> Unit) {
        if(width != size.width || height != size.height){
            width = max(size.width, minWidth)
            height = max(size.height, minHeight)
            consumers.forEach { it(width, height) }
        }
    }

}