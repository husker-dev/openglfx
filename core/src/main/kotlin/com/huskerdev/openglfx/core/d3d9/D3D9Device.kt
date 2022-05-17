package com.huskerdev.openglfx.core.d3d9

import com.huskerdev.openglfx.utils.WinUtils
import com.sun.prism.GraphicsPipeline

class D3D9Device(val handle: Long) {

    companion object {

        val fxDevice: D3D9Device by lazy {
            val pipeline = GraphicsPipeline.getPipeline()
            val resourceFactory = (pipeline::class.java.getDeclaredField("factories").apply { isAccessible = true }[pipeline] as Array<*>)[0]!!
            val context = resourceFactory::class.java.getDeclaredField("context").apply { isAccessible = true }[resourceFactory]
            val pContext = context::class.java.getDeclaredField("pContext").apply { isAccessible = true }[context]
            val device = resourceFactory::class.java.getDeclaredMethod("nGetDevice", Long::class.java).apply { isAccessible = true }.invoke(null, pContext) as Long

            D3D9Device(device)
        }
    }

    fun createTexture(width: Int, height: Int): D3D9Texture {
        val result = WinUtils.createD3DTexture(handle, width, height)
        return D3D9Texture(result[0], result[1])
    }

}