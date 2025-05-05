package com.huskerdev.plugins.compilation

import com.huskerdev.plugins.compilation.types.OutputType
import com.huskerdev.plugins.compilation.types.Platform
import com.huskerdev.plugins.compilation.types.compile
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.os.OperatingSystem
import java.io.File

@Suppress("unused")
class NativePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("java-library")

        project.tasks.withType(JavaCompile::class.java){
            sourceCompatibility = JavaVersion.VERSION_11.toString()
        }

        project.java {
            withJavadocJar()
            withSourcesJar()
        }

        val config = project.extensions.create("compilation", NativeExtension::class.java)

        // Configuration defaults
        config.baseName = "lib"
        config.version = project.version.toString()
        config.classpath = ""
        config.type = OutputType.SHARED
        config.jdk = File(System.getProperty("java.home"))
        if(config.jdk!!.name == "jre")
            config.jdk = config.jdk!!.parentFile
        config.jvmInclude = File(config.jdk, "include")
        config.includeDirs = arrayListOf(project.file("shared"))

        config.windows.srcDirs = arrayListOf(project.file("win"), project.file("shared"))
        config.windows.jvmInclude = File(config.jdk, "include/win32")
        config.windows.useDXSdk = false

        config.macos.srcDirs = arrayListOf(project.file("macos"), project.file("shared"))
        config.macos.jvmInclude = File(config.jdk, "include/darwin")

        config.linux.srcDirs = arrayListOf(project.file("linux"), project.file("shared"))
        config.linux.jvmInclude = File(config.jdk, "include/linux")
        config.linux.pkgConfig = ""

        // Mark source folders
        project.afterEvaluate {
            project.sourceSets {
                create("include").java.srcDirs(
                    *(config.includeDirs +
                    config.windows.includeDirs +
                    config.macos.includeDirs +
                    config.linux.includeDirs).toTypedArray()
                )
                create("win").java.srcDirs(
                    *config.windows.srcDirs.toTypedArray()
                )
                create("macos").java.srcDirs(
                    *config.macos.srcDirs.toTypedArray()
                )
                create("linux").java.srcDirs(
                    *config.linux.srcDirs.toTypedArray()
                )
            }
        }

        // Tasks
        project.pluginManager.withPlugin("maven-publish") {
            project.tasks.register("publishNatives") {
                group = "publishing"
                dependsOn("publishToMavenCentralPortal")
            }
        }

        project.tasks.register("compileNatives") {
            group = "compilation"
            doFirst {
                launchCompile(config, project)
            }
        }

        project.tasks.register("packNatives", Copy::class.java) {
            group = "build"

            dependsOn("compileNatives")

            from(project.nativesBuildDir)
            into(project.sourceSets.getByName("main").output.resourcesDir!!)
        }

        project.tasks.getByName("jar").dependsOn("packNatives")
    }

    private fun launchCompile(config: NativeExtension, project: Project){
        val buildDir = project.nativesBuildDir
        buildDir.deleteRecursively()
        val artifactDir = File(buildDir, config.classpath.replace(".", "/"))
        artifactDir.mkdirs()


        // Platform defaults
        val platform = platform
        if(platform == null)
            throw GradleException("Unknown platform")

        val platformConfig = platform.configGetter(config)

        var architectures = config.architectures.toTypedArray()
        if(architectures.isEmpty() && platformConfig.architectures.isEmpty())
            architectures = platform.defaultArch

        val include = arrayListOf(
            config.jvmInclude!!.absolutePath.replace("\\", "/"),
            platformConfig.jvmInclude!!.absolutePath.replace("\\", "/")
        )
        include += config.includeDirs.map { it.absolutePath }
        include += platformConfig.includeDirs.map { it.absolutePath }

        val libFolders = config.libDirs + platformConfig.libDirs
        val libs = config.libs + platformConfig.libs

        val src = arrayListOf<String>()
        (config.srcDirs + platformConfig.srcDirs).forEach { path ->
            val file = project.file(path)
            if(file.isDirectory)
                project.fileTree(file).files.forEach {
                    if(it.absolutePath.endsWith(".c") ||
                        it.absolutePath.endsWith(".cpp") ||
                        it.absolutePath.endsWith(".m") ||
                        it.absolutePath.endsWith(".mm")
                    ) src += it.absolutePath
                }
            else
                src += file.absolutePath
        }

        val outputName = artifactDir.absolutePath + "/" + config.baseName

        compile(
            platform.compiler,
            config,
            platformConfig,
            project,
            config.type,
            architectures,
            include,
            libFolders.map { it.absolutePath },
            libs,
            outputName,
            src
        )
    }

    private val Project.nativesBuildDir
        get() = File(layout.buildDirectory.get().asFile, "natives")

    private val Project.sourceSets
        get() = extensions.getByType(SourceSetContainer::class.java)

    private fun Project.sourceSets(block: SourceSetContainer.() -> Unit) =
        project.sourceSets.apply(block)

    private fun Project.java(block: JavaPluginExtension.() -> Unit) =
        extensions.getByType(JavaPluginExtension::class.java).apply(block)
}


internal val platform
    get() = Platform.values().firstOrNull {
        it.condition(OperatingSystem.current())
    }

val shortOSName = platform?.shortName ?: "unknown"