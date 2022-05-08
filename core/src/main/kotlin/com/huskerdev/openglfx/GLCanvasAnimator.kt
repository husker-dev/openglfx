package com.huskerdev.openglfx

import java.lang.Thread.sleep
import kotlin.concurrent.thread


class GLCanvasAnimator @JvmOverloads constructor(fps: Double, started: Boolean = false) {

    companion object {
        const val UNLIMITED_FPS = 0.0
    }

    var boundCanvas: OpenGLCanvas? = null
        internal set

    var started = false
        set(value) {
            if(!timerThread.isAlive)
                timerThread.start()
            field = value
            synchronized(timerNotifier) { timerNotifier.notifyAll() }
        }

    private var waitMillis = 0L
    private var waitNanos = 0

    var fps = 60.0
        set(value) {
            field = value

            if(value == UNLIMITED_FPS) {
                waitMillis = 0
                waitNanos = 0
            } else {
                waitMillis = (1000.0 / fps).toLong()
                waitNanos = ((1000.0 / fps - waitMillis) * 100000).toInt()
            }
        }

    private val timerNotifier = Object()
    private val timerThread = thread(start = false, isDaemon = true) {
        while(true){
            if(!started){
                synchronized(timerNotifier) { timerNotifier.wait() }
                continue
            }
            if(fps != UNLIMITED_FPS)
                sleep(waitMillis, waitNanos)
            boundCanvas?.repaint()
        }
    }

    init {
        this.fps = fps
        this.started = started
    }

}