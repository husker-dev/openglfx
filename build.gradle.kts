import com.huskerdev.openglfx.plugins.utils.pom

plugins {
    alias(libs.plugins.pkmerboot.central.publisher) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    id("utils")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    group = "com.huskerdev"
}

pom {
    url = "https://github.com/husker-dev/"
    licenses {
        license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }
    developers {
        developer {
            id = "husker-dev"
            name = "Nikita Shtengauer"
            email = "redfancoestar@gmail.com"
        }
    }
    scm {
        connection = "scm:git:git://github.com/husker-dev/openglfx.git"
        developerConnection = connection
        url = this@pom.url
    }
}