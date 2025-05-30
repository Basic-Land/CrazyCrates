plugins {
    id("paper-plugin")

    alias(libs.plugins.runPaper)
    alias(libs.plugins.shadow)
}

project.group = "${rootProject.group}.paper"
project.version = rootProject.version
project.description = "Add crates to your Paper server with 11 different crate types to choose from!"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    maven("https://repo.triumphteam.dev/snapshots/")

    maven("https://repo.fancyplugins.de/releases/")

    maven("https://repo.nexomc.com/snapshots/")

    maven("https://repo.oraxen.com/releases/")
}

dependencies {
    implementation(project(":crazycrates-core"))

    compileOnly(fileTree("$projectDir/libs/compile").include("*.jar"))

    implementation(libs.triumph.cmds)

    implementation(libs.fusion.paper)

    implementation(libs.metrics)

    compileOnly(libs.bundles.dependencies)
    compileOnly(libs.bundles.shared)
    compileOnly(libs.bundles.crates)

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    compileOnly("net.jpountz.lz4:lz4:1.3.0")
}

tasks {
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")

        listOf(
            "com.ryderbelserion.fusion",
            "org.bstats"
        ).forEach {
            relocate(it, "libs.$it")
        }
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

    processResources {
        inputs.properties("name" to rootProject.name)
        inputs.properties("version" to project.version)
        inputs.properties("group" to project.group)
        inputs.properties("apiVersion" to libs.versions.minecraft.get())
        inputs.properties("description" to project.description)
        inputs.properties("website" to rootProject.properties["website"].toString())

        filesMatching("paper-plugin.yml") {
            expand(inputs.properties)
        }
    }

    runPaper.folia.registerTask()

    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")

        defaultCharacterEncoding = Charsets.UTF_8.name()

        minecraftVersion(libs.versions.minecraft.get())
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class.java).configureEach {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
}