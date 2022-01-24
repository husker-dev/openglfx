package com.huskerdev.openglfx

import java.lang.Thread.sleep
import kotlin.concurrent.thread

class FXGLTimer(fps: Double) {

    lateinit var canvas: OpenGLCanvas

    private var _started = false
    var started: Boolean
        get() = _started
        set(value) {
            if(!this::canvas.isInitialized)
                throw NullPointerException("OpenGLCanvas is not bound to timer")

            if(!timerThread.isAlive)
                timerThread.start()
            _started = value
            synchronized(timerNotifier) { timerNotifier.notifyAll() }
        }

    private var waitMillis = 0L
    private var waitNanos = 0

    private var _fps = 0.0
    var fps: Double
        get() = _fps
        set(value) {
            _fps = value

            waitMillis = (1000.0 / fps).toLong()
            waitNanos = ((1000.0 / fps - waitMillis) * 100000).toInt()
        }

    private val timerNotifier = Object()
    private val timerThread = thread(start = false, isDaemon = true) {
        while(true){
            if(!_started){
                synchronized(timerNotifier) { timerNotifier.wait() }
                continue
            }
            sleep(waitMillis, waitNanos)
            canvas.repaint()
        }
    }

    init {
        this.fps = fps
    }

}