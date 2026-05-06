import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("net.neoforged.moddev") version "2.0.141"
}

val minecraft = stonecutter.current.version
val minecraftTitle = mod.prop("mc_title")
val loader = stonecutter.current.project.substringAfterLast('-')
val javaVersion = mod.prop("java_version")

version = "${mod.version}+$minecraftTitle"
group = mod.group
base {
    archivesName.set("${mod.name}-$loader")
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("src/common/src/main/resources"))
        resources.srcDir(rootProject.file("src/neoforge/src/main/resources"))
    }
}

versionedJavaSources(
    rootProject.file("src/common/src/main/java"),
    rootProject.file("src/neoforge/src/main/java")
)

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/")
}

neoForge {
    version = mod.dep("neoforge_loader")

    runs {
        register("client") {
            gameDirectory = rootProject.file("run")
            client()
        }
    }
}

val requiredJava = JavaVersion.toVersion(javaVersion)

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)
}

val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.jar.get().archiveFile)
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
        dependsOn(tasks.named("clientRun"))
    }
}

tasks.processResources {
    properties(
        listOf("META-INF/neoforge.mods.toml"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "loader" to mod.dep("neoforge_loader_range"),
        "neoforge" to mod.dep("neoforge_version_range")
    )
    properties(
        listOf("*.mixins.json"),
        "java" to javaVersion
    )

    doLast {
        fileTree(outputs.files.singleFile).matching {
            include("**/*.json")
        }.forEach { file ->
            file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
        }
    }
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

stonecutter {
    constants {
        put("fabric", false)
        put("neoforge", true)
    }
}