plugins {
    base
    id("dev.architectury.loom-no-remap") version "1.14-SNAPSHOT" apply false
}

group = providers.gradleProperty("maven_group").get()
version = providers.gradleProperty("mod_version").get()

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.terraformersmc.com/releases/")
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
    }
}

tasks.named("build") {
    dependsOn(":fabric:jar", ":neoforge:jar")
}