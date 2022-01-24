package com.huskerdev.openglfx.utils

import kotlin.concurrent.thread

class LifetimeLoopThread(private val lifetime: Long, private var runnable: Runnable) {

    private var threadInstance = Thread()
    private var startTime = System.currentTimeMillis()

    fun startRequest(){
        startTime = System.currentTimeMillis()
        if(!threadInstance.isAlive){
            threadInstance = thread(isDaemon = true){
                while(System.currentTimeMillis() - startTime < lifetime){
                    Thread.sleep(1)
                    runnable.run()
                }
            }
        }
    }
}