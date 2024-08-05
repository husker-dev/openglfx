package com.huskerdev.openglfx.libgdx

import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.Preferences

// Adaptation of Lwjgl3ApplicationConfiguration
class OGLFXApplicationConfiguration {

    companion object {
        var errorStream = System.err

        fun copy(config: OGLFXApplicationConfiguration): OGLFXApplicationConfiguration {
            val copy = OGLFXApplicationConfiguration()
            copy.set(config)
            return copy
        }
    }

    /** Whether to disable audio or not. If set to true, the returned audio class instances like {@link Audio} or {@link Music}
     * will be mock implementations. */
    var disableAudio = false

    /** The maximum amount of threads to use for network requests. Default is {@link Integer#MAX_VALUE}. */
    var maxNetThreads = Int.MAX_VALUE

    internal var audioDeviceSimultaneousSources = 16
    internal var audioDeviceBufferSize = 512
    internal var audioDeviceBufferCount = 9

    internal var preferencesDirectory = ".prefs/"
    internal var preferencesFileType = FileType.External

    /** Sets the audio device configuration.
     *
     * @param simultaneousSources the maximum amount of sources that can be played simultaneously (default 16)
     * @param bufferSize the audio device buffer size in samples (default 512)
     * @param bufferCount the audio device buffer count (default 9) */
    fun setAudioConfig(simultaneousSources: Int, bufferSize: Int, bufferCount: Int) {
        audioDeviceSimultaneousSources = simultaneousSources
        audioDeviceBufferSize = bufferSize
        audioDeviceBufferCount = bufferCount
    }

    /** Sets the directory where [Preferences] will be stored, as well as the file type to be used to store them. Defaults to
     * "$USER_HOME/.prefs/" and [FileType.External].  */
    fun setPreferencesConfig(preferencesDirectory: String, preferencesFileType: FileType) {
        this.preferencesDirectory = preferencesDirectory
        this.preferencesFileType = preferencesFileType
    }

    fun set(config: OGLFXApplicationConfiguration){
        this.disableAudio = config.disableAudio
        this.maxNetThreads = config.maxNetThreads

        this.audioDeviceSimultaneousSources = config.audioDeviceSimultaneousSources
        this.audioDeviceBufferSize = config.audioDeviceBufferSize
        this.audioDeviceBufferCount = config.audioDeviceBufferCount

        this.preferencesDirectory = config.preferencesDirectory
        this.preferencesFileType = config.preferencesFileType
    }
}