plugins {
    `java-library`

    `maven-publish`
}

repositories {
    maven("https://repo.codemc.io/repository/maven-public/")

    maven("https://repo.crazycrew.us/releases/")

    maven("https://jitpack.io/")

    maven("http://nexus.basicland.cz:8081/repository/dev-private/") {
        isAllowInsecureProtocol = true
        credentials {
            username = "dev"
            password = "rtVXgxFyWkiVfU3"
        }
    }

    flatDir { dirs("libs") }

    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}