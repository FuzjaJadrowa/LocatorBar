plugins {
    `java-library`
    id("net.fabricmc.fabric-loom-companion") version "1.14-SNAPSHOT"
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}