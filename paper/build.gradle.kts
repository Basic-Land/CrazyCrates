plugins {
    `config-paper`
}

project.group = "${rootProject.group}.paper"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    maven("https://repo.fancyinnovations.com/releases/")

    maven("https://repo.nexomc.com/releases/")

    maven("https://repo.oraxen.com/releases/")
}

dependencies {
    implementation(project(":crazycrates-core"))

    compileOnly(fileTree("$projectDir/libs/compile").include("*.jar"))

    implementation(libs.triumph.cmds)

    implementation(libs.fusion.paper)

    implementation(libs.metrics)

    compileOnly(libs.bundles.holograms)
    compileOnly(libs.bundles.shared)
    compileOnly(libs.bundles.crates)

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    compileOnly("net.jpountz.lz4:lz4:1.3.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        listOf(
            "org.bstats"
        ).forEach {
            relocate(it, "libs.$it")
        }
    }

    runPaper.folia.registerTask()

    runServer {
        jvmArgs("-Dnet.kyori.ansi.colorLevel=truecolor")
        jvmArgs("-Dcom.mojang.eula.agree=true")

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