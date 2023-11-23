package com.huskerdev.openglfx.internal

internal data class Size<A>(var sizeWidth: A, var sizeHeight: A) {

    inline fun changeOnDifference(newWidth: A, newHeight: A, consumer: Size<A>.() -> Unit){
        if(sizeWidth != newWidth || sizeHeight != newHeight){
            sizeWidth = newWidth
            sizeHeight = newHeight
            consumer(this)
        }
    }

    inline fun changeOnDifference(size: Size<A>, consumer: Size<A>.() -> Unit) =
        changeOnDifference(size.sizeWidth, size.sizeHeight, consumer)
}