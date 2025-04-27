plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.huskerdev.openglfx.plugins.properties"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
}

gradlePlugin {
    plugins {
        plugins.create("properties") {
            id = name
            implementationClass = "com.huskerdev.plugins.properties.PropertiesPlugin"
        }
    }
}

