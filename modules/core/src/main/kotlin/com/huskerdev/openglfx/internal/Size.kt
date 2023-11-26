package com.huskerdev.openglfx.internal

internal data class Size(
    var width: Int = Integer.MIN_VALUE,
    var height: Int = Integer.MIN_VALUE
) {

    inline fun executeOnDifferenceWith(newWidth: Int, newHeight: Int, consumer: (sizeWidth: Int, sizeHeight: Int) -> Unit){
        if(width != newWidth || height != newHeight){
            width = newWidth
            height = newHeight
            consumer(width, height)
        }
    }

    inline fun executeOnDifferenceWith(size: Size, consumer: (sizeWidth: Int, sizeHeight: Int) -> Unit) =
        this.executeOnDifferenceWith(size.width, size.height, consumer)

    fun executeOnDifferenceWith(size: Size, vararg consumers: (sizeWidth: Int, sizeHeight: Int) -> Unit) {
        if(width != size.width || height != size.height){
            width = size.width
            height = size.height
            consumers.forEach { it(width, height) }
        }
    }

}