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

    compileOnly(libs.lwjgl2)
}

pom {
    name = "$rootName-lwjgl2"
    description = "$rootName lwjgl2 module"
}
