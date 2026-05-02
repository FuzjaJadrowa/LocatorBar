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
    implementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    compileOnly("com.terraformersmc:modmenu:${rootProject.property("modmenu_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
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
