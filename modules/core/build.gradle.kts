import com.huskerdev.openglfx.plugins.utils.*
import com.huskerdev.plugins.maven.silentApi


plugins {
    alias(libs.plugins.kotlin.jvm)
    id("utils")
    id("maven")
    id("properties")
    id("module-info")
}

configureKotlinProject()

dependencies {
    compileOnly(*expandFXPlatforms(libs.bundles.javafx))
    api(libs.grapl.gl)

    silentApi("$group:$rootName-natives-core-win:$version")
    silentApi("$group:$rootName-natives-core-linux:$version")
    silentApi("$group:$rootName-natives-core-macos:$version")
}

pom {
    name = rootName
    description = "$rootName core module"
}

properties {
    name = "GLFXInfo"
    classpath = "com.huskerdev.openglfx"
    srcDir = file("kotlin")

    field("VERSION", version)
}

moduleInfo {
    name = rootName
    requiresTransitive(
        "kotlin.stdlib",
        "grapl.gl",
        "grapl",
    )
    requires(
        "javafx.base",
        "javafx.graphics"
    )
    exports(
        "com.huskerdev.openglfx",
        "com.huskerdev.openglfx.canvas",
        "com.huskerdev.openglfx.canvas.events",
        "com.huskerdev.openglfx.image",
        "com.huskerdev.openglfx.internal",
        "com.huskerdev.openglfx.internal.canvas",
        "com.huskerdev.openglfx.internal.platforms",
        "com.huskerdev.openglfx.internal.platforms.win",
        "com.huskerdev.openglfx.internal.platforms.macos",
        "com.huskerdev.openglfx.internal.shaders"
    )
}
