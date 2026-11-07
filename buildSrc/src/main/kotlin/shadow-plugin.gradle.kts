plugins {
    id("com.gradleup.shadow")
    id("java-plugin")
    `maven-publish`
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        exclude("META-INF/**")
    }
}

publishing {
    repositories {
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

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${rootProject.group}" // us.crazycrew.crazycrates
            artifactId = rootProject.name
            version = rootProject.version.toString() + "-SNAPSHOT"

            artifact(tasks.shadowJar)
        }
    }
}