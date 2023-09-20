package com.huskerdev.openglfx.jogl.example.scene

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

fun useDirectIntBuffer(capacity: Int, returnIndex: Int, applier: IntBuffer.() -> Unit) =
    createDirectIntBuffer(capacity).apply(applier)[returnIndex]

fun createDirectByteBuffer(capacity: Int) =
    ByteBuffer.allocateDirect(capacity)
    .order(ByteOrder.nativeOrder())

fun createDirectIntBuffer(capacity: Int) =
    createDirectByteBuffer(capacity * Int.SIZE_BYTES)
        .asIntBuffer()

fun FloatArray.toDirectBuffer() =
    createDirectByteBuffer(this.size * Float.SIZE_BYTES)
        .asFloatBuffer()
        .apply { put(this@toDirectBuffer); flip() }

fun IntArray.toDirectBuffer() =
    createDirectByteBuffer(this.size * Int.SIZE_BYTES)
        .asIntBuffer()
        .apply { put(this@toDirectBuffer); flip() }
