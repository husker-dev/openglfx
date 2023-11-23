package com.huskerdev.openglfx.internal

class FPSCounter {

    // Delta
    private var lastDeltaTime = System.nanoTime()
    var delta = 0.0
        private set

    // Fps
    private var lastFpsTime = System.nanoTime()
    private var countedFps = 0
    var currentFps = 0
        private set

    var frameId = 0L
        private set

    internal fun update(){
        val now = System.nanoTime()
        delta = (now - lastDeltaTime) / 1000000000.0
        lastDeltaTime = now

        countedFps++
        frameId++
        if(now - lastFpsTime >= 1000000000) {
            currentFps = countedFps
            countedFps = 0
            lastFpsTime = now
        }

    }
}