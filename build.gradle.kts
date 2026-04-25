plugins {
    base
    id("dev.architectury.loom") version "1.14-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
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
    dependsOn(":fabric:remapJar", ":neoforge:remapJar")
}