plugins {
    id("dev.architectury.loom-no-remap")
}

base {
    archivesName.set("${rootProject.property("archives_name")}-${project.name}+${rootProject.property("minecraft_version")}")
}

loom {
    runs {
        named("client") {
            client()
        }
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:${rootProject.property("minecraft_version")}")
    add("neoForge", "net.neoforged:neoforge:${rootProject.property("neoforge_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(mapOf("version" to project.version))
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}