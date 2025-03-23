pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
}


rootProject.name = "CrazyCrates"

listOf(
    "examples/paper" to "example-paper",

    "publish" to "publish",

    "paper" to "paper",
    "core" to "core",
    "api" to "api"
).forEach(::includeProject)

fun includeProject(pair: Pair<String, String>): Unit = includeProject(pair.first, pair.second)

fun includeProject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}

fun includeProject(path: String, name: String) {
    includeProject(name) {
        this.name = "${rootProject.name.lowercase()}-$name"
        this.projectDir = File(path)
    }
}

fun includeProject(name: String) {
    includeProject(name) {
        this.name = "${rootProject.name.lowercase()}-$name"
    }
}