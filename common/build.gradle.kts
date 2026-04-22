plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    `maven-publish`
}

architectury {
    minecraft = rootProject.property("minecraft_version").toString()
    common(rootProject.property("enabled_platforms").toString().split(","))
}

base {
    archivesName.set("${rootProject.property("archives_name")}-${project.name}")
}

dependencies {
    minecraft("net.minecraft:minecraft:${rootProject.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
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