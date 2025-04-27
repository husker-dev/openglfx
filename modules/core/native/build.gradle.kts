import com.huskerdev.openglfx.plugins.utils.*
import com.huskerdev.plugins.compilation.*


plugins {
    id("utils")
    id("native-compilation")
    id("maven")
    id("module-info")
}

compilation {
    baseName = "lib"
    classpath = "com.huskerdev.openglfx.natives.$shortOSName"

    includeDirs = arrayListOf(file("shared"), file("include"))

    windows {
        libs = arrayListOf("user32", "gdi32", "d3d9")
    }
    macos {
        frameworks = arrayListOf("Cocoa", "IOSurface", "OpenGL")
    }
}

pom {
    name = "$rootName-natives-core-$shortOSName"
    description = "$rootName core module (natives for $shortOSName)"
}

moduleInfo {
    name = "$rootName.natives.core.$shortOSName"
    opens = arrayListOf(
        "com.huskerdev.openglfx.natives.$shortOSName"
    )
}

