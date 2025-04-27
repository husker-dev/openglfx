package com.huskerdev.plugins.compilation.utils

import org.gradle.api.GradleException
import java.io.File

data class VSType(
    val rootDrive: File,
    val type: String,
    val version: String,
    val file: File
) {
    override fun toString() =
        file.toString()
}

fun findVCVarsDir(): String{
    val vcPath = arrayListOf<VSType>()
    for(root in File.listRoots()){
        for(path in arrayOf(
            "Microsoft Visual Studio",
            "Program Files/Microsoft Visual Studio",
            "Program Files (x86)/Microsoft Visual Studio"
        )){
            for(type in arrayOf("Community", "Enterprise")){
                for(version in arrayOf("2022", "2019")){
                    val file = File(root, "$path/$version/$type")
                    if(file.exists())
                        vcPath.add(VSType(root, type, version, file))
                }
            }
        }
    }
    if(vcPath.isEmpty())
        throw GradleException("Cant find Visual Studio directory")
    vcPath.sortedBy { it.file.toString() }
    return File(vcPath.last().file, "VC/Auxiliary/Build").absolutePath
}

fun findWinSDKDir(): String{
    for(root in File.listRoots()){
        for(path in arrayOf(
            "Windows Kits",
            "Program Files/Windows Kits",
            "Program Files (x86)/Windows Kits",
        )){
            val file = File(root, "$path/10")
            if(file.exists())
                return file.absolutePath
        }
    }
    throw GradleException("Cant find any Windows SDK 10")
}

fun findWinSDKVersion(): String{
    for(dir in File(findWinSDKDir(), "Lib").listFiles())
        if(File(dir, "um").exists() && File(dir, "ucrt").exists())
            return dir.name
    throw GradleException("Cant find any version of Windows SDK 10")
}

fun findDXSDKDir(): String{
    for(root in File.listRoots()){
        for(path in arrayOf(
            "Program Files",
            "Program Files (x86)",
        )){
            val children = File(root, path).listFiles()
            if(children != null){
                for(child in children){
                    if(child.name.contains("Microsoft DirectX SDK"))
                        return child.absolutePath
                }
            }
        }
    }
    throw GradleException("Cant find any DirectX SDK")
}