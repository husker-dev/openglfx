package com.huskerdev.openglfx.libgdx.internal

import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.backends.lwjgl3.audio.Lwjgl3Audio
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio
import com.badlogic.gdx.utils.Clipboard
import com.huskerdev.openglfx.libgdx.OGLFXApplicationConfiguration

class OGLFXApplication(val config: OGLFXApplicationConfiguration): Application {
    companion object {
        init {
            Lwjgl3NativesLoader.load()
        }
    }

    private val audio: Lwjgl3Audio
    private val files: Files
    private val net: Net
    private val clipboard: Lwjgl3Clipboard

    init {
        applicationLogger = Lwjgl3ApplicationLogger()

        this.audio = if (!config.disableAudio) {
            try {
                OpenALLwjgl3Audio(
                    config.audioDeviceSimultaneousSources,
                    config.audioDeviceBufferCount,
                    config.audioDeviceBufferSize
                )
            } catch (t: Throwable) {
                log("OGLFXApplication", "Couldn't initialize audio, disabling audio", t)
                MockAudio()
            }
        } else MockAudio()

        files = Lwjgl3Files()
        net = Lwjgl3Net(Lwjgl3ApplicationConfiguration().apply { setMaxNetThreads(config.maxNetThreads) })
        this.clipboard = Lwjgl3Clipboard()

        Gdx.app = this
        Gdx.audio = audio
        Gdx.files = files
        Gdx.net = net
    }


    override fun getApplicationListener(): ApplicationListener {
        TODO("Not yet implemented")
    }

    override fun getGraphics(): Graphics {
        TODO("Not yet implemented")
    }

    override fun getAudio() = audio

    override fun getInput(): Input {
        TODO("Not yet implemented")
    }

    override fun getFiles() = files

    override fun getNet() = net

    override fun log(tag: String?, message: String?) {
        TODO("Not yet implemented")
    }

    override fun log(tag: String?, message: String?, exception: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun error(tag: String?, message: String?) {
        TODO("Not yet implemented")
    }

    override fun error(tag: String?, message: String?, exception: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun debug(tag: String?, message: String?) {
        TODO("Not yet implemented")
    }

    override fun debug(tag: String?, message: String?, exception: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun setLogLevel(logLevel: Int) {
        TODO("Not yet implemented")
    }

    override fun getLogLevel(): Int {
        TODO("Not yet implemented")
    }

    override fun setApplicationLogger(applicationLogger: ApplicationLogger?) {
        TODO("Not yet implemented")
    }

    override fun getApplicationLogger(): ApplicationLogger {
        TODO("Not yet implemented")
    }

    override fun getType(): Application.ApplicationType {
        TODO("Not yet implemented")
    }

    override fun getVersion(): Int {
        TODO("Not yet implemented")
    }

    override fun getJavaHeap(): Long {
        TODO("Not yet implemented")
    }

    override fun getNativeHeap(): Long {
        TODO("Not yet implemented")
    }

    override fun getPreferences(name: String?): Preferences {
        TODO("Not yet implemented")
    }

    override fun getClipboard() = clipboard

    override fun postRunnable(runnable: Runnable?) {
        TODO("Not yet implemented")
    }

    override fun exit() {}

    override fun addLifecycleListener(listener: LifecycleListener?) {
        TODO("Not yet implemented")
    }

    override fun removeLifecycleListener(listener: LifecycleListener?) {
        TODO("Not yet implemented")
    }
}