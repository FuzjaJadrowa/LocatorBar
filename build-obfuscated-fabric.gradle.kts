import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
}

val minecraft = stonecutter.current.version
val minecraftTitle = mod.prop("mc_title")
val loader = stonecutter.current.project.substringAfterLast('-')
val minecraftDependency = mod.dep("minecraft.fabric")
val javaVersion = mod.prop("java_version")

version = "${mod.version}+$minecraftTitle"
group = mod.group
base {
    archivesName.set("${mod.name}-$loader")
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("src/common/src/main/resources"))
        resources.srcDir(rootProject.file("src/fabric/src/main/resources"))
    }
}

versionedJavaSources(
    rootProject.file("src/common/src/main/java"),
    rootProject.file("src/fabric/src/main/java")
)

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftDependency")
    fun implement(dependency: String) {
        modImplementation(dependency)
    }

    implement("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
    implement("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_api_version")}")
    modCompileOnly("com.terraformersmc:modmenu:${mod.dep("modmenu_version")}")
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
    from(tasks.remapJar.get().archiveFile)
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
        put("fabric", true)
        put("neoforge", false)
    }
}