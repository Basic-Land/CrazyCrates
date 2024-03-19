pluginManagement {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")

        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")

        gradlePluginPortal()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library("paperweight", "io.papermc.paperweight", "paperweight-userdev").version("1.5.11")

            library("shadow", "com.github.johnrengelman", "shadow").version("8.1.1")

            library("runpaper", "xyz.jpenilla", "run-task").version("2.2.3")

            version("adventure4", "4.16.0")

            library("holographicdisplays", "me.filoghost.holographicdisplays", "holographicdisplays-api").version("3.0.0")

            library("decentholograms", "com.github.decentsoftware-eu", "decentholograms").version("2.8.6")

            library("triumphcmds", "dev.triumphteam", "triumph-cmd-bukkit").version("2.0.0-ALPHA-9")
            library("triumphgui", "dev.triumphteam", "triumph-gui").version("3.1.7")

            library("simpleyaml", "com.github.Carleslc.Simple-YAML", "Simple-Yaml").version("1.8.4")

            library("adventure4", "net.kyori", "adventure-text-minimessage").versionRef("adventure4")
            library("minimessage4", "net.kyori", "adventure-api").versionRef("adventure4")

            library("simpleyaml", "com.github.Carleslc.Simple-YAML", "Simple-Yaml").version("1.8.4")

            library("arcaniax", "com.arcaniax", "HeadDatabase-API").version("1.3.0")

            library("itemsadder", "com.github.LoneDev6", "api-itemsadder").version("3.6.1")

            library("placeholderapi", "me.clip", "placeholderapi").version("2.11.5")
            library("vault", "com.github.MilkBowl", "VaultAPI").version("1.7.1")
            library("metrics", "org.bstats", "bstats-bukkit").version("3.0.2")
            library("nbtapi", "de.tr7zw", "item-nbt-api").version("2.12.2")
            library("oraxen", "io.th0rgal", "oraxen").version("1.164.0")
            library("configme", "ch.jalu", "configme").version("1.4.1")

            bundle("adventure", listOf("adventure4", "minimessage4"))
        }
    }
}

rootProject.name = "CrazyCrates"

include("api")
include("paper")
include("cluster")
include("cluster:api")
include("cluster:paper")
include("cluster:buildSrc")