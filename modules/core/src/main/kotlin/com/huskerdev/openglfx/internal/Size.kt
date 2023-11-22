package com.huskerdev.openglfx.internal

internal data class Size<A>(var width: A, var height: A) {

    inline fun onDifference(newWidth: A, newHeight: A, consumer: () -> Unit){
        if(width != newWidth || height != newHeight){
            width = newWidth
            height = newHeight
            consumer()
        }
    }
}