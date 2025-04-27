plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.huskerdev.openglfx.plugins.maven"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")

    implementation("io.gitee.pkmer:central-publisher:1.1.1")
}

gradlePlugin {
    plugins {
        plugins.create("maven") {
            id = name
            implementationClass = "com.huskerdev.plugins.maven.MavenPlugin"
        }
    }
}

