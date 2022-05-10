package com.huskerdev.openglfx.utils

import com.sun.javafx.PlatformUtil
import com.sun.prism.GraphicsPipeline
import sun.misc.Unsafe
import java.nio.ByteBuffer
import java.util.concurrent.Executor




class OpenGLFXUtils {

    companion object {
        private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }[null] as Unsafe

        private var macosDispatcher: Executor? = null

        val pipelineName: String
            get() = GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]

        fun cleanByteBuffer(buffer: ByteBuffer) = unsafe.invokeCleaner(buffer)

        fun executeOnMainThread(runnable: Runnable){
            if(PlatformUtil.isMac()){
                if(macosDispatcher == null){
                    macosDispatcher = try {
                        val dispatchClass = Class.forName("com.apple.concurrent.Dispatch")
                        val dispatchInstance = dispatchClass.getMethod("getInstance").invoke(null)
                        dispatchClass.getMethod("getNonBlockingMainQueueExecutor").invoke(dispatchInstance) as Executor
                    } catch (throwable: Throwable) {
                        throw RuntimeException("Could not reflectively access Dispatch", throwable)
                    }
                }
                macosDispatcher!!.execute(runnable)
            }else runnable.run()
        }
    }
}