package com.huskerdev.openglfx.libgdx.internal

import com.badlogic.gdx.graphics.Pixmap
import javafx.scene.image.PixelFormat
import javafx.scene.image.PixelReader
import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import javafx.scene.paint.Color
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.IntBuffer


class FXPixmapImage(pixmap: Pixmap): WritableImage(object: PixelReader {

    override fun getPixelFormat() = PixelFormat.getIntArgbInstance()

    override fun getArgb(x: Int, y: Int) = when(pixmap.format){
        Pixmap.Format.Alpha, Pixmap.Format.Intensity, Pixmap.Format.LuminanceAlpha ->
            0x00ffffff or (pixmap.getPixel(x, y) shl 24)
        Pixmap.Format.RGB888 ->
            pixmap.getPixel(x, y) or 0xff000000.toInt()
        Pixmap.Format.RGBA8888 -> {
            pixmap.getPixel(x, y) shr 8 or(pixmap.getPixel(x, y) shl 24)
        }
        Pixmap.Format.RGB565, Pixmap.Format.RGBA4444, null -> TODO()
    }

    override fun getColor(x: Int, y: Int): Color {
        val intColor = getArgb(x, y)
        return Color(
            (intColor shr 16 and 0x000000ff) / 255.0,
            (intColor shr 8 and 0x000000ff) / 255.0,
            (intColor and 0x000000ff) / 255.0,
            (intColor shr 24 and 0x000000ff) / 255.0)
    }

    override fun <T : Buffer?> getPixels(
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        pixelformat: WritablePixelFormat<T>?,
        buffer: T,
        scanlineStride: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun getPixels(
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        pixelformat: WritablePixelFormat<ByteBuffer>?,
        buffer: ByteArray,
        offset: Int,
        scanlineStride: Int
    ) {
        var index = offset
        for(xi in x until w){
            for(yi in y until h){
                val color = getColor(xi, yi)
                buffer[index++] = (color.opacity * 255).toInt().toByte()
                buffer[index++] = (color.red * 255).toInt().toByte()
                buffer[index++] = (color.green * 255).toInt().toByte()
                buffer[index++] = (color.blue * 255).toInt().toByte()
            }
        }
    }

    override fun getPixels(
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        pixelformat: WritablePixelFormat<IntBuffer>?,
        buffer: IntArray,
        offset: Int,
        scanlineStride: Int
    ) {
        var index = offset
        for(xi in x until w){
            for(yi in y until h){
                buffer[index++] = getArgb(x, y)
            }
        }
    }
}, pixmap.width, pixmap.height)