plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

architectury {
    minecraft = rootProject.property("minecraft_version").toString()
    platformSetupLoomIde()
    fabric()
}

base {
    archivesName.set("${rootProject.property("archives_name")}-${project.name}")
}

val common by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

configurations.named("compileClasspath") {
    extendsFrom(common)
}
configurations.named("runtimeClasspath") {
    extendsFrom(common)
}
configurations.named("developmentFabric") {
    extendsFrom(common)
}

val shadowBundle by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    minecraft("net.minecraft:minecraft:${rootProject.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    modCompileOnly("com.terraformersmc:modmenu:${rootProject.property("modmenu_version")}")

    val commonDependency = add("common", project(mapOf("path" to ":common", "configuration" to "namedElements")))
    (commonDependency as org.gradle.api.artifacts.ModuleDependency).isTransitive = false
    add("shadowBundle", project(mapOf("path" to ":common", "configuration" to "transformProductionFabric")))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("dev-shadow")
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    inputFile.set(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").flatMap { it.archiveFile })
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}