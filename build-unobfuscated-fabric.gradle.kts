plugins {
    id("dev.architectury.loom-no-remap") version "1.14-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin")
}

val loader = stonecutter.current.project.substringAfterLast('-')
val minecraftDependency = mod.dep("minecraft.fabric")
val javaVersion = mod.prop("java_version")

configureLocatorBarCommon(
    loader = loader,
    isActive = stonecutter.current.isActive,
    archiveTaskName = "jar"
)

dependencies {
    minecraft("com.mojang:minecraft:$minecraftDependency")
    fun implement(dependency: String) {
        implementation(dependency)
    }

    implement("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
    implement("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_api_version")}")
    compileOnly("com.terraformersmc:modmenu:${mod.dep("modmenu_version")}")
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)
}

loom {
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
        listOf("fabric.mod.json"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "java" to javaVersion,
        "fabric_loader" to mod.dep("fabric_loader")
    )
}

stonecutter {
    constants {
        put("fabric", true)
        put("neoforge", false)
    }
}

configureLocatorBarPublishing()