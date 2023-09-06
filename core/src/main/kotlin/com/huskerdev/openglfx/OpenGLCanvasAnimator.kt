package com.huskerdev.openglfx

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class OpenGLCanvasAnimator @JvmOverloads constructor(
    val fps: Double = UNLIMITED_FPS
) {

    companion object {
        const val UNLIMITED_FPS = 0.0
    }

    private lateinit var executor: ScheduledExecutorService
    private lateinit var timer: ScheduledFuture<*>

    var boundCanvas: OpenGLCanvas? = null
        internal set(value) {
            field = value

            if(this::executor.isInitialized){
                timer.cancel(true)
                executor.shutdown()
            }

            if(value != null) {
                executor = Executors.newScheduledThreadPool(1) { Thread(it).apply { isDaemon = true } }
                timer = executor.scheduleAtFixedRate({
                    boundCanvas?.repaint()
                }, 0, (1000000000 / fps).toLong(), TimeUnit.NANOSECONDS)
            }
        }

    internal fun bind(canvas: OpenGLCanvas?){
        boundCanvas = canvas
    }

    internal fun unbind(){
        boundCanvas = null
    }
}