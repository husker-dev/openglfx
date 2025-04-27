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

    compileOnly(libs.gdx)
    compileOnly(libs.gdx.backend.lwjgl3)
    compileOnly(variantOf(libs.gdx.platform){ classifier("natives-desktop") })
}

pom {
    name = "$rootName-libgdx"
    description = "$rootName libgdx module"
}

moduleInfo {
    name = "$rootName.libgdx"
    requiresTransitive = arrayListOf(
        "openglfx"
    )
    requires = arrayListOf(
        "org.lwjgl",
        "org.lwjgl.opengl",
        "javafx.controls"
    )
    exports = arrayListOf(
            "com.huskerdev.openglfx.libgdx",
            "com.huskerdev.openglfx.libgdx.events",
            "com.huskerdev.openglfx.libgdx.internal"
    )
}