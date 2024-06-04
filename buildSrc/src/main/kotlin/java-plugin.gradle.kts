import com.ryderbelserion.feather.enums.Repository
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("com.ryderbelserion.feather-core")

    `maven-publish`

    `java-library`
}

repositories {
    flatDir { dirs("libs") }

    maven("http://nexus.basicland.cz:8081/repository/dev-private/") {
        isAllowInsecureProtocol = true
        credentials {
            username = "dev"
            password = "rtVXgxFyWkiVfU3"
        }
    }

    mavenCentral()
    //mavenLocal()
}

dependencies {
    compileOnlyApi(libs.annotations)
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

feather {
    repository("https://repo.codemc.io/repository/maven-public")

    repository(Repository.CrazyCrewReleases.url)

    repository(Repository.Jitpack.url)
}