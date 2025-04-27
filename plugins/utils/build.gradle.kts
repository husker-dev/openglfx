plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.huskerdev.openglfx.plugins.utils"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
}

gradlePlugin {
    plugins {
        plugins.create("utils") {
            id = name
            implementationClass = "com.huskerdev.openglfx.plugins.utils.UtilsPlugin"
        }
    }
}

