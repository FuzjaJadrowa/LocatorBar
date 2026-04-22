import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Jar

plugins {
    base
    id("dev.architectury.loom") version "1.14-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

group = providers.gradleProperty("maven_group").get()
version = providers.gradleProperty("mod_version").get()

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
    }
}

val archivesName = providers.gradleProperty("archives_name")
val modVersion = providers.gradleProperty("mod_version")
val fabricRemapJar = project(":fabric").layout.buildDirectory.file("libs/${archivesName.get()}-fabric-${modVersion.get()}.jar")
val neoforgeRemapJar = project(":neoforge").layout.buildDirectory.file("libs/${archivesName.get()}-neoforge-${modVersion.get()}.jar")

tasks.register<Jar>("multiloaderJar") {
    group = "build"
    description = "Builds a single universal jar for Fabric and NeoForge."
    dependsOn(":fabric:remapJar", ":neoforge:remapJar")

    archiveBaseName.set("${archivesName.get()}-fabric-neoforge")
    archiveVersion.set(modVersion.get())
    destinationDirectory.set(layout.buildDirectory.dir("libs"))

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    from(zipTree(fabricRemapJar))
    from(zipTree(neoforgeRemapJar))
}

tasks.named("build") {
    dependsOn("multiloaderJar")
}