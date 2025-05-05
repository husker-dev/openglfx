package com.huskerdev.plugins.moduleinfo

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes
import java.io.File

@Suppress("unused")
class ModuleInfoPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val config = project.extensions.create("moduleInfo", ModuleInfoExtension::class.java)
        config.name = "unnamed"
        config.open = false

        project.tasks.register("createModuleInfo") {
            doFirst {
                val classWriter = ClassWriter(0)
                classWriter.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null)

                val moduleVisitor = classWriter.visitModule(
                    config.name,
                    if(config.open) Opcodes.ACC_OPEN else 0,
                    null)
                moduleVisitor.visitRequire("java.base", 0, null)

                config.requires.forEach {
                    moduleVisitor.visitRequire(it, 0, null)
                }
                config.requiresTransitive.forEach {
                    moduleVisitor.visitRequire(it, Opcodes.ACC_TRANSITIVE, null)
                }
                config.requiresStatic.forEach {
                    moduleVisitor.visitRequire(it, Opcodes.ACC_STATIC_PHASE, null)
                }
                config.exports.forEach {
                    moduleVisitor.visitExport(it.replace(".", "/"), 0)
                }
                config.opens.forEach {
                    moduleVisitor.visitOpen(it.replace(".", "/"), 0)
                }

                moduleVisitor.visitEnd()
                classWriter.visitEnd()

                project.moduleInfoBuildDir.mkdirs()
                File(project.moduleInfoBuildDir, "module-info.class").writeBytes(classWriter.toByteArray())
            }
        }

        project.tasks.register("packModuleInfo", Copy::class.java) {
            group = "build"

            dependsOn("createModuleInfo")

            from(project.moduleInfoBuildDir)
            into(project.sourceSets.getByName("main").output.resourcesDir!!)
        }

        project.tasks.getByName("jar").dependsOn("packModuleInfo")
        project.tasks.getByName("javadoc").mustRunAfter("packModuleInfo")

        project.afterEvaluate {
            project.tasks.named("jar", Jar::class.java) {
                manifest {
                    attributes(mapOf("Automatic-Module-Name" to config.name))
                }
            }
        }
    }


    private val Project.moduleInfoBuildDir
        get() = File(layout.buildDirectory.get().asFile, "moduleinfo")

    private val Project.sourceSets
        get() = extensions.getByType(SourceSetContainer::class.java)

}