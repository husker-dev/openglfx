package com.huskerdev.plugins.compilation.types

import com.huskerdev.plugins.compilation.AbstractPlatformConfiguration
import com.huskerdev.plugins.compilation.LinuxConfiguration
import com.huskerdev.plugins.compilation.MacosConfiguration
import com.huskerdev.plugins.compilation.NativeExtension
import com.huskerdev.plugins.compilation.WindowsConfiguration
import com.huskerdev.plugins.compilation.platform
import com.huskerdev.plugins.compilation.utils.execute
import com.huskerdev.plugins.compilation.utils.findDXSDKDir
import com.huskerdev.plugins.compilation.utils.findVCVarsDir
import com.huskerdev.plugins.compilation.utils.findWinSDKDir
import com.huskerdev.plugins.compilation.utils.findWinSDKVersion
import org.gradle.api.Project
import java.io.File

enum class Compiler {
    CL, GCC, CLANG
}

fun compile(
    compiler: Compiler,
    projectConfig: NativeExtension,
    platformConfig: AbstractPlatformConfiguration,
    project: Project,
    type: OutputType,
    archs: Array<Arch>,
    includeDirs: List<String>,
    libDirs: List<String>,
    libs: List<String>,
    output: String,
    sources: List<String>
){
    when(compiler){
        Compiler.CL -> {
            platformConfig as WindowsConfiguration

            val tmpDir = File(project.layout.buildDirectory.get().asFile, "tmp/native")
            tmpDir.mkdirs()

            val varsDir = platformConfig.varsDir ?: findVCVarsDir()
            val sdkDir = platformConfig.sdkDir ?: findWinSDKDir()

            val sdkVersion = findWinSDKVersion()
            val newIncludeDirs = includeDirs.toMutableList()
            newIncludeDirs += "${sdkDir}/Include/${sdkVersion}/ucrt"
            newIncludeDirs += "${sdkDir}/Include/${sdkVersion}/um"
            newIncludeDirs += "${sdkDir}/Include/${sdkVersion}/shared"
            newIncludeDirs += "${sdkDir}/Include/${sdkVersion}/winrt"

            if(platformConfig.useDXSdk)
                newIncludeDirs += "${findDXSDKDir()}/Include"

            archs.forEach { arch ->
                val typeModifier = if(type == OutputType.SHARED) "/LD" else ""
                val archLibs = arrayListOf(
                    "${sdkDir}/Lib/${sdkVersion}/ucrt/${arch.name.lowercase()}",
                    "${sdkDir}/Lib/${sdkVersion}/um/${arch.name.lowercase()}"
                )
                if(platformConfig.useDXSdk)
                    archLibs += "${findDXSDKDir()}/Lib/${arch.name.lowercase()}"

                val varsFile = when(arch){
                    Arch.X86 -> "vcvars32"
                    Arch.X64 -> "vcvars64"
                    Arch.ARM64 -> "vcvars_arm64"
                }

                while(true) {
                    try {
                        execute(arrayOf(
                            "call \"${varsDir}/${varsFile}.bat\"",
                            """cl
                            $typeModifier
                            /EHsc /O1
                            ${sources.joinToString(" ")}
                            ${libs.joinToString(" ") { "${it}.lib" }}
                            ${newIncludeDirs.joinToString(" ") { "/I\"$it\"" }}
                            /link
                            /MACHINE:${arch.name}
                            ${(libDirs + archLibs).joinToString(" ") { "/LIBPATH:\"$it\"" }}
                            /out:${getOutputName(projectConfig, output, type, arch)}
                            """
                        ),
                            varsDir,
                            tmpDir
                        )
                    } catch (e: Exception) {
                        if ("-1073741819" in e.message!!)
                            continue
                        else throw e
                    }
                    break
                }
            }
        }
        Compiler.GCC -> {
            platformConfig as LinuxConfiguration

            val typeModifier = if(type == OutputType.SHARED)
                "-shared" else ""

            archs.forEach { arch ->
                val archModifier = when(arch){
                    Arch.X86 -> "-m32"
                    Arch.X64 -> "-m64"
                    Arch.ARM64 -> "-arm64"
                }

                execute(arrayOf("""g++
                        -Wall -Os -s
                        -Wno-unused-function
                        -Wno-unused-variable
                        -fPIC
                        $typeModifier
                        $archModifier
                        ${includeDirs.joinToString(" ") { "-I$it" }}
                        ${libDirs.joinToString(" ") { "-L$it" }}
                        -o ${getOutputName(projectConfig, output, type, arch)}
                        ${sources.joinToString(" ")}
                        ${platformConfig.pkgConfig}
                        ${libs.joinToString(" ") { "-l$it" }}
                        """),
                    "",
                    project.file(".")
                )
            }
        }
        Compiler.CLANG -> {
            platformConfig as MacosConfiguration
            val typeModifier = if(type == OutputType.SHARED)
                "-shared" else ""

            var archModifier = ""
            if(Arch.ARM64 in archs)
                archModifier += "-arch arm64 "
            if(Arch.X64 in archs)
                archModifier += "-arch x86_64 "
            execute(arrayOf("""clang++
                    -Wall -Os -v
                    -std=c++0x
                    -fmodules
                    $typeModifier
                    $archModifier
                    ${platformConfig.frameworks.joinToString(" ") { "-framework $it" }}
                    ${includeDirs.joinToString(" ") { "-I$it" }}
                    ${libDirs.joinToString(" ") { "-L$it" }}
                    -o ${getOutputName(projectConfig, output, type, null)}
                    ${sources.joinToString(" ")}
                    ${libs.joinToString(" ") { "-l$it" }}
                    """),
                "",
                project.file(".")
            )
        }
    }
}

private fun getOutputName(
    projectConfig: NativeExtension,
    output: String,
    type: OutputType,
    arch: Arch?
): String{
    var result = output
    if(projectConfig.version != "")
        result += "-" + projectConfig.version
    if(arch != null)
        result += "-" + arch.name.lowercase()

    val platform = platform!!
    return result + when (type){
        OutputType.EXECUTABLE -> platform.executableExt
        OutputType.SHARED -> platform.sharedExt
        OutputType.STATIC -> platform.staticExt
    }
}

