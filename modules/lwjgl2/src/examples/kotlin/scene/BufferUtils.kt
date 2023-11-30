package scene

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun createDirectByteBuffer(capacity: Int) =
    ByteBuffer.allocateDirect(capacity)
    .order(ByteOrder.nativeOrder())

fun FloatArray.toDirectBuffer() =
    createDirectByteBuffer(this.size * Float.SIZE_BYTES)
        .asFloatBuffer()
        .apply { put(this@toDirectBuffer); flip() }

fun IntArray.toDirectBuffer() =
    createDirectByteBuffer(this.size * Int.SIZE_BYTES)
        .asIntBuffer()
        .apply { put(this@toDirectBuffer); flip() }
