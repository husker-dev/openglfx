import com.huskerdev.openglfx.plugins.utils.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("utils")
    id("maven")
    id("module-info")
}

configureKotlinProject()

dependencies {
    compileOnly(*expandFXPlatforms(libs.bundles.javafx))
    api(project(":modules:core"))

    compileOnly(platform(libs.lwjgl.bom))
    compileOnly(libs.lwjgl)
    compileOnly(libs.lwjgl.opengl)

    arrayOf("linux", "linux-arm64", "macos", "macos-arm64", "windows", "windows-x86", "windows-arm64").forEach {
        compileOnly(variantOf(libs.lwjgl){ classifier("natives-$it") })
        compileOnly(variantOf(libs.lwjgl.opengl){ classifier("natives-$it") })
    }
}

pom {
    name = "$rootName-lwjgl"
    description = "$rootName lwjgl module"
}

moduleInfo {
    name = "$rootName.lwjgl"
    requiresTransitive = arrayListOf(
        "openglfx"
    )
    requires = arrayListOf(
        "org.lwjgl",
        "org.lwjgl.opengl"
    )
    exports = arrayListOf(
        "com.huskerdev.openglfx.lwjgl"
    )
}