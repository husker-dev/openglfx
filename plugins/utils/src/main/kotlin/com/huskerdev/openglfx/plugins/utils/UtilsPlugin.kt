package com.huskerdev.openglfx.plugins.utils

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import kotlin.collections.flatMap

@Suppress("unused")
class UtilsPlugin: Plugin<Project> {
    override fun apply(project: Project) = Unit
}


fun Project.configureKotlinProject(){
    sourceSets["main"].java.srcDir("kotlin")
    sourceSets["main"].resources.srcDir("resources")
    sourceSets["test"].resources.srcDir("test")

    kotlin {
        jvmToolchain(11)
    }
    java {
        withJavadocJar()
        withSourcesJar()
    }

    project.tasks.getByName("jar", Jar::class)
        .duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    this.dependencies.add("api", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

fun expandFXPlatforms(modules: Provider<ExternalModuleDependencyBundle>) =
    modules.get().flatMap { lib ->
        listOf("linux", "linux-aarch64", "mac", "mac-aarch64", "win").map {
            "${lib.module}:${lib.version}:$it"
        }
    }.toTypedArray()

fun DependencyHandler.compileOnly(vararg deps: String) {
    deps.forEach {
        add("compileOnly", it)
    }
}

fun Project.pom(block: Action<MavenPom>){
    this.extensions.getByType(ExtraPropertiesExtension::class.java)
        .set("pom", block)
}

val Project.rootName
    get() = rootProject.name

private val Project.sourceSets
    get() = extensions.getByType(SourceSetContainer::class.java)

private fun Project.kotlin(block: KotlinJvmProjectExtension.() -> Unit) =
    extensions.getByType(KotlinJvmProjectExtension::class.java).apply(block)

private fun Project.java(block: JavaPluginExtension.() -> Unit) =
    extensions.getByType(JavaPluginExtension::class.java).apply(block)

