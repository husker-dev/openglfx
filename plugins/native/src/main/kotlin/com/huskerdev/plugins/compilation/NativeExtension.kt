package com.huskerdev.plugins.compilation

import com.huskerdev.plugins.compilation.types.Arch
import com.huskerdev.plugins.compilation.types.OutputType
import java.io.File

@Suppress("unused")
open class NativeExtension: AbstractPlatformConfiguration() {
    var baseName: String = ""
    var version: String = ""
    var classpath: String = ""

    var type: OutputType = OutputType.SHARED
    var jdk: File? = null

    var windows = WindowsConfiguration()
    var linux = LinuxConfiguration()
    var macos = MacosConfiguration()

    fun windows(block: WindowsConfiguration.() -> Unit) =
        windows.apply(block)

    fun linux(block: LinuxConfiguration.() -> Unit) =
        linux.apply(block)

    fun macos(block: MacosConfiguration.() -> Unit) =
        macos.apply(block)
}

@Suppress("unused")
open class AbstractPlatformConfiguration {
    var jvmInclude: File? = null
    var includeDirs: ArrayList<File> = arrayListOf()
    var srcDirs: ArrayList<File> = arrayListOf()
    var libDirs: ArrayList<File> = arrayListOf()
    var libs: ArrayList<String> = arrayListOf()
    var architectures: ArrayList<Arch> = arrayListOf()

    fun include(dir: File) {
        includeDirs.add(dir)
    }

    fun src(dir: File) {
        srcDirs.add(dir)
    }
}

open class WindowsConfiguration: AbstractPlatformConfiguration() {
    var varsDir: String? = null
    var sdkDir: String? = null
    var useDXSdk: Boolean = false
}

open class MacosConfiguration: AbstractPlatformConfiguration() {
    var frameworks: ArrayList<String> = arrayListOf()
}

open class LinuxConfiguration: AbstractPlatformConfiguration() {
    var pkgConfig: String? = null
}