rootProject.name = "openglfx"

pluginManagement {
    includeBuild("plugins/utils")
    includeBuild("plugins/maven")
    includeBuild("plugins/properties")
    includeBuild("plugins/moduleInfo")
    includeBuild("plugins/native")
}

include (
    "modules:core",
    "modules:core:native",/*
    'modules:jogl',
    'modules:libgdx',
    'modules:lwjgl',
    'modules:lwjgl2'*/
)

