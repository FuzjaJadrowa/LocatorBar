pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
    kotlinController = true
    shared {
        fun mc(loader: String, vararg versions: String) {
            for (version in versions) {
                val buildscript = when {
                    sc.eval(version, ">= 26.1") && loader == "fabric" -> "build-unobfuscated-fabric.gradle.kts"
                    sc.eval(version, ">= 26.1") && loader == "neoforge" -> "build-unobfuscated-neoforge.gradle.kts"
                    loader == "fabric" -> "build-obfuscated-fabric.gradle.kts"
                    loader == "neoforge" -> "build-obfuscated-neoforge.gradle.kts"
                    loader == "forge" -> "build-obfuscated-forge.gradle.kts"
                    else -> error("Unsupported loader: $loader")
                }
                version("$version-$loader", version).buildscript(buildscript)
            }
        }
        mc("fabric", "1.21.1", "1.21.11", "26.1.2", "26.3")
        mc("neoforge", "1.21.1", "1.21.11", "26.1.2", "26.3")
        mc("forge", "1.20.1")
    }
    create(rootProject)
}

rootProject.name = "LocatorBar"