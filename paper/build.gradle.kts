plugins {
    alias(libs.plugins.paperweight)
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.runPaper)

    `paper-plugin`
}

feather {
    repository("https://repo.fancyplugins.de/releases")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper)

    implementation(libs.triumph.cmds)

    implementation(libs.vital.paper)

    compileOnly(fileTree("$projectDir/libs/compile").include("*.jar"))

    compileOnly(libs.decent.holograms)

    compileOnly(libs.fancy.holograms)

    api(projects.crazycratesCore)

    compileOnly("cz.basicland:bLibs:2.0.0-WIP-b31")

    compileOnly ("org.projectlombok:lombok:1.18.32")
    annotationProcessor ("org.projectlombok:lombok:1.18.32")
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

tasks {
    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")

        defaultCharacterEncoding = Charsets.UTF_8.name()

        minecraftVersion("1.20.6")
    }

    assemble {
        dependsOn(shadowJar)

        doLast {
            copy {
                from(shadowJar.get())
                into(rootProject.projectDir.resolve("jars"))
            }
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")

        listOf(
            "com.ryderbelserion",
            "dev.triumphteam",
            "ch.jalu"
        ).forEach {
            relocate(it, "libs.$it")
        }
    }

    processResources {
        inputs.properties("name" to rootProject.name)
        inputs.properties("version" to project.version)
        inputs.properties("group" to project.group)
        inputs.properties("description" to project.properties["description"])
        inputs.properties("website" to project.properties["website"])

        filesMatching("paper-plugin.yml") {
            expand(inputs.properties)
        }
    }
}