package com.husker.openglfx.utils

class LifetimeLoopThread(private val lifetime: Long, private var runnable: Runnable) {

    private var threadInstance = Thread()
    private var startTime = System.currentTimeMillis()

    fun startRequest(){
        startTime = System.currentTimeMillis()
        if(!threadInstance.isAlive){
            threadInstance = Thread{
                while(System.currentTimeMillis() - startTime < lifetime){
                    Thread.sleep(1)
                    runnable.run()
                }
            }
            threadInstance.start()
        }
    }
}