import com.huskerdev.openglfx.plugins.utils.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("utils")
    id("maven")
    id("module-info")
}

repositories {
    maven("https://jogamp.org/deployment/maven/")
}

configureKotlinProject()

dependencies {
    api(project(":modules:core"))
    compileOnly(*expandFXPlatforms(libs.bundles.javafx))

    compileOnly(libs.gluegen)
    compileOnly(libs.jogl)

    arrayOf("linux-amd64", "linux-aarch64", "macosx-universal", "windows-amd64").forEach {
        compileOnly(variantOf(libs.gluegen){ classifier("natives-$it") })
        compileOnly(variantOf(libs.jogl){ classifier("natives-$it") })
    }
}

pom {
    name = "$rootName-jogl"
    description = "$rootName jogl module"
}

moduleInfo {
    name = "$rootName.jogl"
    requiresTransitive = arrayListOf(
        "openglfx"
    )
    exports = arrayListOf(
            "com.huskerdev.openglfx.jogl",
            "com.huskerdev.openglfx.jogl.events"
    )
}