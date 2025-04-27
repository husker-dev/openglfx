package com.huskerdev.plugins.properties

import java.io.File

open class PropertiesExtension {
    var name: String = ""
    var srcDir: File? = null
    var classpath: String = ""

    var fields: Map<String, Any> = hashMapOf()

    fun field(name: String, value: Any){
        fields += name to value
    }
}