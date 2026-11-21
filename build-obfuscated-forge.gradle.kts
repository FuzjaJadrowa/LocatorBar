plugins {
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin")
}

val loader = stonecutter.current.project.substringAfterLast('-')
val minecraftDependency = mod.dep("minecraft.forge")
val javaVersion = mod.prop("java_version")

configureLocatorBarCommon(
    loader = loader,
    isActive = stonecutter.current.isActive,
    archiveTaskName = "remapJar"
)

dependencies {
    minecraft("com.mojang:minecraft:$minecraftDependency")
    mappings(loom.officialMojangMappings())
    add("forge", "net.minecraftforge:forge:${mod.dep("minecraft.forge")}-${mod.dep("forge_loader")}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.remapJar {
    inputs.file(tasks.jar.get().archiveFile)
    archiveClassifier = null
    dependsOn(tasks.jar)
}

tasks.jar {
    archiveClassifier = "dev"
}

loom {
    forge {
        mixinConfigs.add("locatorbar.common.mixins.json")
    }
    runs {
        named("client") {
            runDir = project.projectDir.toPath()
                .relativize(rootProject.file("run/${project.name}/client").toPath())
                .toString()
        }
        named("server") {
            runDir = project.projectDir.toPath()
                .relativize(rootProject.file("run/${project.name}/server").toPath())
                .toString()
        }
    }
}

tasks.processResources {
    properties(
        listOf("META-INF/mods.toml"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "loader" to mod.dep("forge_loader_range"),
        "forge" to mod.dep("forge_version_range")
    )
}

stonecutter {
    constants {
        put("fabric", false)
        put("neoforge", false)
        put("forge", true)
    }
}

configureLocatorBarPublishing()