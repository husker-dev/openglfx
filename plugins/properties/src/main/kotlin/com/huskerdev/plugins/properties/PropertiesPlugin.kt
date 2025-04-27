package com.huskerdev.plugins.properties

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.math.BigDecimal

@Suppress("unused")
class PropertiesPlugin: Plugin<Project> {
    override fun apply(project: Project) {
       val config = project.extensions.create("properties", PropertiesExtension::class.java)

        project.afterEvaluate {
            createFile(config)
        }

        project.tasks.register("generatePropertyFile") {
            doFirst {
                createFile(config)
            }
        }
        project.tasks.getByName("compileJava").dependsOn(
            project.tasks.getByName("generatePropertyFile")
        )
    }

    private fun createFile(config: PropertiesExtension){
        if(config.name == null ||
            config.classpath == null ||
            config.srcDir == null
        ) return
        val file = File(config.srcDir, "${config.classpath!!.replace(".", "/")}/${config.name}.kt")
        file.parentFile.mkdirs()

        val content = config.fields.map { entry ->
            var field = ""

            field += "const val ${entry.key} = "
            field += if(entry.value is Int || entry.value is Double || entry.value is BigDecimal || entry.value is Boolean)
                entry.value
            else if(entry.value is Float)
                "${entry.value}f"
            else if(entry.value is Long)
                "${entry.value}L"
            else "\"${entry.value}\""
            field
        }

        file.writeText("""
            package ${config.classpath}
            
            // Generated with gradle
            class ${config.name} {
                companion object {
                    ${content.joinToString("\n        ")}
                }
            }
        """.trimIndent())
    }
}