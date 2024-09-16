plugins {
    alias(libs.plugins.paperweight)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.shadow)

    `paper-plugin`
}

repositories {
    maven("https://repo.fancyplugins.de/releases")
    maven {
        url = uri("http://nexus.basicland.cz:8081/repository/dev-private/")
        isAllowInsecureProtocol = true
        credentials {
            username = ("dev")
            password = ("rtVXgxFyWkiVfU3")
        }
    }
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper)

    compileOnly(fileTree("$projectDir/libs/compile").include("*.jar"))

    implementation(libs.triumph.cmds)

    implementation(libs.vital.paper) {
        exclude("org.yaml")
    }

    implementation(project(":api"))

    compileOnly(libs.excellentcrates)
    compileOnly(libs.nightcore)

    compileOnly(libs.decent.holograms)

    compileOnly(libs.fancy.holograms)

    compileOnly(libs.headdatabaseapi)

    compileOnly(libs.placeholderapi)

    compileOnly(libs.oraxen)

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    compileOnly("cz.basicland:bLibs:2.0.0-WIP-b38")
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

tasks {
    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")

        defaultCharacterEncoding = Charsets.UTF_8.name()

        minecraftVersion(libs.versions.minecraft.get())
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
            "com.ryderbelserion.vital",
            "dev.triumphteam.cmd"
        ).forEach {
            relocate(it, "libs.$it")
        }
    }

    processResources {
        inputs.properties("name" to rootProject.name)
        inputs.properties("version" to project.version)
        inputs.properties("group" to project.group)
        inputs.properties("apiVersion" to libs.versions.minecraft.get())
        inputs.properties("description" to project.properties["description"])
        inputs.properties("website" to project.properties["website"])

        filesMatching("paper-plugin.yml") {
            expand(inputs.properties)
        }
    }
}