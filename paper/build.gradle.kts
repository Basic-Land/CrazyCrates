plugins {
    `paper-plugin`
}

project.group = "${rootProject.group}.paper"

repositories {
    maven("https://repo.fancyinnovations.com/releases/")

    maven("https://repo.nexomc.com/releases/")

    maven("https://repo.oraxen.com/releases/")

    maven {
        name = "local"
        url = uri("http://192.168.1.103:30451/repository/dev-snapshots/")
        isAllowInsecureProtocol = true
        credentials {
            username = "${System.getenv("NEXUS_USERNAME")}";
            password = "${System.getenv("NEXUS_PASSWORD")}"
        }
    }
}

dependencies {
    implementation(project(":common"))
    if (System.getenv("USERDOMAIN_ROAMINGPROFILE") == "DESKTOP-KPL18HN") {
        compileOnly(fileTree("$projectDir/libs/compile").include("*.jar"))
    } else {
        compileOnly(libs.bLibs)
    }

    implementation(libs.triumph.cmds)

    implementation(libs.fusion.paper)

    implementation(libs.metrics)

    compileOnly(libs.bundles.holograms)
    compileOnly(libs.bundles.shared)
    compileOnly(libs.bundles.crates)

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    //compileOnly("net.jpountz.lz4:lz4:1.3.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        listOf(
            "com.ryderbelserion.fusion",
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