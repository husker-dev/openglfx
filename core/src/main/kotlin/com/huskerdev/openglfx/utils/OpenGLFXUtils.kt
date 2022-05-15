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

        fun executeOnMainThreadSync(runnable: Runnable){
            var notifier: Object? = Object()

            executeOnMainThread {
                runnable.run()

                synchronized(notifier!!) { notifier!!.notifyAll() }
                notifier = null
            }

            if(notifier != null)
                synchronized(notifier!!) { notifier!!.wait() }
        }

        private val arch = System.getProperty("os.arch")
        val arm64 = arch.startsWith("aarch64") || arch.startsWith("armv8")
        val arm32 = arch.startsWith("arm") && !arm64
        val x64 = arch.contains("64") && !arm32 && !arm64
        val x86 = !x64 && !arm32 && !arm64

        val archName: String
            get() = if(arm64) "arm64"
                else if(arm32) "arm32"
                else if(x64) "x64"
                else if(x86) "x86"
                else throw UnsupportedOperationException("Unsupported OS architecture")
    }
}