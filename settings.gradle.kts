pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

rootProject.name = "locatorbar"

include("fabric")
include("neoforge")