plugins {
    alias(libs.plugins.run.paper)

    `paper-plugin`
}

val mcVersion = libs.versions.bundle.get()

dependencies {
    compileOnly(fileTree("$rootDir/libs/compile").include("*.jar"))

    implementation(project(":api"))

    implementation(libs.bundles.triumph)

    implementation(libs.config.me)

    implementation(libs.metrics)

    implementation(libs.vital)

    compileOnly(libs.head.database.api)

    compileOnly(libs.decent.holograms)

    compileOnly(libs.placeholder.api)

    compileOnly(libs.itemsadder.api)

    compileOnly(libs.oraxen.api)

    compileOnly ("org.projectlombok:lombok:1.18.26")
    annotationProcessor ("org.projectlombok:lombok:1.18.26")
}

tasks {
    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")

        defaultCharacterEncoding = Charsets.UTF_8.name()

        minecraftVersion(mcVersion)
    }

    assemble {
        dependsOn(reobfJar)
    }

    reobfJar {
        outputJar = rootProject.projectDir.resolve("jars").resolve("${rootProject.name}-${rootProject.version}.jar")
    }

    shadowJar {
        archiveClassifier.set("")

        listOf(
            "dev.triumphteam",
            "org.bstats",
            "ch.jalu"
        ).forEach {
            relocate(it, "libs.$it")
        }
    }

    processResources {
        val properties = hashMapOf(
            "name" to rootProject.name,
            "version" to rootProject.version,
            "group" to rootProject.group,
            "description" to rootProject.description,
            "apiVersion" to providers.gradleProperty("apiVersion").get(),
            "authors" to providers.gradleProperty("authors").get(),
            "website" to providers.gradleProperty("website").get()
        )

        inputs.properties(properties)

        filesMatching("plugin.yml") {
            expand(properties)
        }
    }
}