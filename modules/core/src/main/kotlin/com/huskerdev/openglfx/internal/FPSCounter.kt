package com.huskerdev.openglfx.internal

internal class FPSCounter {

    // Delta
    private var lastDeltaTime = System.nanoTime()
    var delta = 0.0

    // Fps
    private var lastFpsTime = System.nanoTime()
    private var countedFps = 0
    var currentFps = 0

    fun update(){
        val now = System.nanoTime()
        delta = (now - lastDeltaTime) / 1000000000.0
        lastDeltaTime = now

        countedFps++
        if((now - lastFpsTime) / 1000000 > 1000) {
            currentFps = countedFps
            countedFps = 0
            lastFpsTime = now
        }
    }
}