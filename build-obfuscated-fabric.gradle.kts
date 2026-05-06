import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
}

val minecraft = stonecutter.current.version
val loader = stonecutter.current.project.substringAfterLast('-')
val minecraftDependency = mod.dep("minecraft.$loader")
val javaVersion = mod.prop("java_version")

version = "${mod.version}+$minecraft"
group = mod.group
base {
    archivesName.set("${mod.id}-$loader")
}

sourceSets {
    main {
        java.srcDir(rootProject.file("src/common/src/main/java"))
        resources.srcDir(rootProject.file("src/common/src/main/resources"))
        java.srcDir(rootProject.file("src/$loader/src/main/java"))
        resources.srcDir(rootProject.file("src/$loader/src/main/resources"))
    }
}

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftDependency")
    fun implement(dependency: String) {
        modImplementation(dependency)
    }

    if (loader == "fabric") {
        implement("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
        implement("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_api_version")}")
        modCompileOnly("com.terraformersmc:modmenu:${mod.dep("modmenu_version")}")
    }
    if (loader == "neoforge") {
        "neoForge"("net.neoforged:neoforge:${mod.dep("neoforge_loader")}")
    }
    mappings(loom.officialMojangMappings())
}

val requiredJava = JavaVersion.toVersion(javaVersion)

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks.remapJar {
    inputs.file(tasks.jar.get().archiveFile)
    archiveClassifier = null
    dependsOn(tasks.jar)
}

tasks.jar {
    archiveClassifier = "dev"
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(buildAndCollect)
    }

    rootProject.tasks.register("runActive") {
        group = "project"
        dependsOn(tasks.named("runClient"))
    }
}

tasks.processResources {
    properties(
        listOf("fabric.mod.json"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "java" to javaVersion,
        "fabric_loader" to mod.dep("fabric_loader")
    )
    properties(
        listOf("*.mixins.json"),
        "java" to javaVersion
    )
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

tasks.processResources {
    doLast {
        fileTree(outputs.files.singleFile).matching {
            include("**/*.json")
        }.forEach { file ->
            file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
        }
    }
}

stonecutter {
    constants {
        arrayOf("fabric", "neoforge").forEach { it -> put(it, loader == it) }
    }
}
