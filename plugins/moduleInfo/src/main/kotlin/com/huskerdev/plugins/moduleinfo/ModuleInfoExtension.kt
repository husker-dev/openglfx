package com.huskerdev.plugins.moduleinfo

@Suppress("unused")
open class ModuleInfoExtension {
    var name: String = ""
    var open: Boolean = false

    var exports: ArrayList<String> = arrayListOf()
    var opens: ArrayList<String> = arrayListOf()
    var requires: ArrayList<String> = arrayListOf()
    var requiresTransitive: ArrayList<String> = arrayListOf()
    var requiresStatic: ArrayList<String> = arrayListOf()

    fun exports(vararg elements: String) =
        exports.addAll(elements)

    fun opens(vararg elements: String) =
        opens.addAll(elements)

    fun requires(vararg elements: String) =
        requires.addAll(elements)

    fun requiresTransitive(vararg elements: String) =
        requiresTransitive.addAll(elements)

    fun requiresStatic(vararg elements: String) =
        requiresStatic.addAll(elements)
}