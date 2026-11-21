plugins {
    id("net.neoforged.moddev") version "2.0.147"
    id("me.modmuss50.mod-publish-plugin")
}

val loader = stonecutter.current.project.substringAfterLast('-')
val projectName = project.name

configureLocatorBarCommon(
    loader = loader,
    isActive = stonecutter.current.isActive,
    archiveTaskName = "jar",
    clientRunTask = "clientRun",
    serverRunTask = "serverRun"
)

neoForge {
    version = mod.dep("neoforge_loader")

    mods {
        register(mod.id) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = rootProject.file("run/$projectName/client")
            client()
        }
        register("server") {
            gameDirectory = rootProject.file("run/$projectName/server")
            server()
        }
    }
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)
}

tasks.processResources {
    properties(
        listOf("META-INF/neoforge.mods.toml"),
        "id" to mod.id,
        "name" to mod.name,
        "iconProperty" to "logoFile",
        "version" to mod.version,
        "minecraft" to mod.prop("mc_targets"),
        "loader" to mod.dep("neoforge_loader_range"),
        "neoforge" to mod.dep("neoforge_version_range")
    )
}

stonecutter {
    constants {
        put("fabric", false)
        put("neoforge", true)
    }
}

configureLocatorBarPublishing()