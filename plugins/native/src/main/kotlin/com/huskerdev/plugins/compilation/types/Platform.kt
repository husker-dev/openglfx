package com.huskerdev.plugins.compilation.types

import com.huskerdev.plugins.compilation.AbstractPlatformConfiguration
import com.huskerdev.plugins.compilation.NativeExtension
import org.gradle.internal.os.OperatingSystem

enum class Platform(
    val shortName: String,
    val condition: (OperatingSystem) -> Boolean,
    val configGetter: (NativeExtension) -> AbstractPlatformConfiguration,
    val compiler: Compiler,
    val defaultArch: Array<Arch>,
    val staticExt: String,
    val sharedExt: String,
    val executableExt: String
) {
    WINDOWS(
        "win",
        { it.isWindows },
        { it.windows },
        Compiler.CL,
        arrayOf(Arch.X64, Arch.X86),
        ".lib", ".dll", ".exe"
    ),
    LINUX(
        "linux",
        { it.isLinux },
        { it.linux },
        Compiler.GCC,
        arrayOf(Arch.X64, Arch.X86),
        ".a", ".so", ""
    ),
    MACOS(
        "macos",
        { it.isMacOsX },
        { it.macos },
        Compiler.CLANG,
        arrayOf(Arch.X64, Arch.ARM64),
        ".a", ".dylib", ""
    )
}