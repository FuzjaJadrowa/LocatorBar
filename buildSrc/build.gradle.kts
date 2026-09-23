plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.3.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("me.modmuss50:mod-publish-plugin:1.1.0")
    testImplementation(kotlin("test-junit"))
    testImplementation("com.google.code.gson:gson:2.11.0")
}

sourceSets.test {
    java.srcDir("../src/common/src/main/java")
    java.include("pl/fuzjajadrowa/locatorbar/config/**", "pl/fuzjajadrowa/locatorbar/util/MarkerMath.java")
}

tasks.test {
    useJUnit()
    systemProperty("locatorbar.root", projectDir.parentFile.absolutePath)
    workingDir = layout.buildDirectory.dir("test-work").get().asFile
    doFirst { workingDir.mkdirs() }
    inputs.dir("../src")
    inputs.file("../settings.gradle.kts")
}
