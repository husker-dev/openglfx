package com.huskerdev.openglfx.utils

import sun.misc.Unsafe
import java.nio.ByteBuffer



class BufferUtilization {

    companion object {
        private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }[null] as Unsafe

        fun clean(buffer: ByteBuffer) = unsafe.invokeCleaner(buffer)
    }
}