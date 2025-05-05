package com.huskerdev.plugins.compilation.utils

import org.gradle.api.GradleException
import org.gradle.internal.os.OperatingSystem
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

fun execute(command: Array<String>, envPath: String, directory: File){
    val commandStr = command.joinToString("\n") {
        it.split("\n").joinToString(" ") { it.trim() }
    }
    println(commandStr)

    val file: File
    if(OperatingSystem.current().isWindows){
        file = File(directory, "script.bat")
        file.writeText(commandStr)
    }else {
        file = File(directory, "script.sh")
        file.writeText("#!/bin/sh\n$commandStr")
        Runtime.getRuntime().exec("chmod +x ${file.absolutePath}").waitFor()
    }

    val builder = ProcessBuilder(file.absolutePath)
        .directory(directory)
        .redirectErrorStream(true)
    builder.environment()["Path"] = envPath

    val process = builder.start()
    val r = BufferedReader(InputStreamReader(process.inputStream))
    var line = r.readLine()
    while (line != null) {
        println(line)
        line = r.readLine()
    }

    val result = process.waitFor()
    file.delete()
    if(result != 0)
        throw GradleException("Execution of '${file.absolutePath}' finished with exit code: ${process.exitValue()}")
}