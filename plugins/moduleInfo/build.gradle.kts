plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.huskerdev.openglfx.plugins.moduleinfo"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")

    implementation("org.ow2.asm:asm:9.5")
}

gradlePlugin {
    plugins {
        plugins.create("module-info") {
            id = name
            implementationClass = "com.huskerdev.plugins.moduleinfo.ModuleInfoPlugin"
        }
    }
}

