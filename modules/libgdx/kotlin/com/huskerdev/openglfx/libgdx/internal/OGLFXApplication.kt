package com.huskerdev.openglfx.libgdx.internal

import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio
import com.badlogic.gdx.utils.Clipboard
import com.badlogic.gdx.utils.ObjectMap
import com.huskerdev.openglfx.libgdx.LibGDXCanvas
import com.huskerdev.openglfx.libgdx.OGLFXApplicationConfiguration
import java.io.File

class OGLFXApplication(
    private val config: OGLFXApplicationConfiguration,
    val canvas: LibGDXCanvas
): Application {
    companion object {
        init {
            Lwjgl3NativesLoader.load()
        }
    }

    private val preferences = ObjectMap<String, Preferences>()

    private val audio: Audio
    private val files: Files
    private val net: Net
    private val clipboard: Clipboard
    private val graphics: Graphics
    private val input: Input
    private var logger: ApplicationLogger
    private var logLevel = Application.LOG_INFO

    private val defaultListener = object: ApplicationListener{
        override fun create() = canvas.fireInitEvent()
        override fun resize(width: Int, height: Int) = canvas.fireReshapeEvent(width, height)
        override fun render() = canvas.fireRenderEvent(0)
        override fun dispose() = canvas.fireDisposeEvent()
        override fun pause() {}
        override fun resume() {}
    }

    init {
        logger = Lwjgl3ApplicationLogger()

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
        clipboard = OGLFXClipboard()
        graphics = OGLFXGraphics(canvas)
        input = OGLFXInput(canvas)

        Gdx.app = this
        Gdx.audio = audio
        Gdx.files = files
        Gdx.net = net
        Gdx.graphics = graphics
        Gdx.input = input
    }

    override fun getApplicationListener() = defaultListener

    override fun getGraphics() = graphics
    override fun getAudio() = audio
    override fun getInput() = input
    override fun getFiles() = files
    override fun getNet() = net

    override fun debug(tag: String?, message: String?) {
        if (logLevel >= Application.LOG_DEBUG) logger.debug(tag, message)
    }

    override fun debug(tag: String?, message: String?, exception: Throwable?) {
        if (logLevel >= Application.LOG_DEBUG) logger.debug(tag, message, exception)
    }

    override fun log(tag: String?, message: String?) {
        if (logLevel >= Application.LOG_INFO) logger.log(tag, message)
    }

    override fun log(tag: String?, message: String?, exception: Throwable?) {
        if (logLevel >= Application.LOG_INFO) logger.log(tag, message, exception)
    }

    override fun error(tag: String?, message: String?) {
        if (logLevel >= Application.LOG_ERROR) logger.error(tag, message)
    }

    override fun error(tag: String?, message: String?, exception: Throwable?) {
        if (logLevel >= Application.LOG_ERROR) logger.error(tag, message, exception)
    }

    override fun setLogLevel(logLevel: Int) {
        this.logLevel = logLevel
    }

    override fun getLogLevel() = logLevel

    override fun setApplicationLogger(applicationLogger: ApplicationLogger) {
        logger = applicationLogger
    }

    override fun getApplicationLogger() = logger

    override fun getType() = Application.ApplicationType.Desktop

    override fun getVersion() = 1

    override fun getJavaHeap() = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

    override fun getNativeHeap() = javaHeap

    override fun getPreferences(name: String): Preferences =
        if (preferences.containsKey(name)) {
            preferences.get(name)
        } else {
            val prefs = Lwjgl3Preferences(Lwjgl3FileHandle(File(config.preferencesDirectory, name), config.preferencesFileType))
            preferences.put(name, prefs)
            prefs
        }

    override fun getClipboard() = clipboard

    override fun postRunnable(runnable: Runnable) =
        canvas.invokeLater(runnable)

    override fun exit() {}

    override fun addLifecycleListener(listener: LifecycleListener?) {

    }

    override fun removeLifecycleListener(listener: LifecycleListener?) {

    }
}