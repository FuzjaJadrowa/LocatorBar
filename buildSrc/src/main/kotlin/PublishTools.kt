import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType.STABLE
import me.modmuss50.mpp.platforms.curseforge.CurseforgeOptions
import me.modmuss50.mpp.platforms.modrinth.ModrinthOptions
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

private const val MODRINTH_PROJECT_ID_PROPERTY = "publish.modrinth.project_id"
private const val CURSEFORGE_PROJECT_ID_PROPERTY = "publish.curseforge.project_id"

fun Project.configureLocatorBarPublishing() {
    configureRootGithubPublishing()

    val loader = name.substringAfterLast('-')
    val target = name.substringBeforeLast('-')
    val loaderTitle = loader.upperCaseFirst()
    val minecraftTitle = mod.prop("mc_title")
    val minecraftTargets = mod.prop("mc_targets")
    val releaseDisplayName = "Release ${mod.version} for $loaderTitle $minecraftTitle"
    val supportedMinecraftVersions = publishedMinecraftVersions(minecraftTargets, mod.dep("minecraft.$loader"))
    val changelog = providers.fileContents(
        rootProject.layout.projectDirectory.file(".github/changelogs/$target-$loader-changelog.md")
    ).asText

    extensions.configure<ModPublishExtension>("publishMods") {
        file.set(publishJarTaskName(loader).let { taskName ->
            tasks.named<AbstractArchiveTask>(taskName).flatMap { it.archiveFile }
        })
        this.changelog.set(changelog)
        type.set(STABLE)
        version.set("${mod.version}+$minecraftTitle-$loader")
        displayName.set(releaseDisplayName)
        modLoaders.add(loader)

        modrinth {
            accessToken.set(providers.environmentVariable("MODRINTH_API_KEY"))
            projectId.set(providerPropertyOrEnvironment(MODRINTH_PROJECT_ID_PROPERTY, "MODRINTH_PROJECT_ID"))
            configureMinecraftVersions(supportedMinecraftVersions)
            if (loader == "fabric") {
                requires("fabric-api")
                optional("modmenu")
            }
        }

        curseforge {
            accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
            projectId.set(providerPropertyOrEnvironment(CURSEFORGE_PROJECT_ID_PROPERTY, "CURSEFORGE_PROJECT_ID"))
            configureMinecraftVersions(supportedMinecraftVersions)
            clientRequired.set(true)
            serverRequired.set(true)
            this.changelog.set(changelog)
            changelogType.set("markdown")
            if (loader == "fabric") {
                requires("fabric-api")
                optional("modmenu")
            }
        }
    }
}

private fun Project.configureRootGithubPublishing() {
    val configuredMarker = "locatorbar.publish.root_configured"
    if (rootProject.extensions.extraProperties.has(configuredMarker)) {
        return
    }
    rootProject.extensions.extraProperties.set(configuredMarker, true)
    rootProject.plugins.apply("me.modmuss50.mod-publish-plugin")

    val modVersion = rootProject.providers.gradleProperty("mod.version")
    rootProject.extensions.configure<ModPublishExtension>("publishMods") {
        changelog.set(
            rootProject.providers.fileContents(
                rootProject.layout.projectDirectory.file(".github/changelogs/matrix-changelog.md")
            ).asText
        )
        type.set(STABLE)
        version.set(modVersion)
        displayName.set(modVersion.map { "v$it" })

        github {
            accessToken.set(rootProject.providers.environmentVariable("GITHUB_TOKEN"))
            repository.set(
                rootProject.providers.gradleProperty("publish.github.repository")
                    .orElse(rootProject.providers.environmentVariable("GITHUB_REPOSITORY"))
                    .orElse("FuzjaJadrowa/LocatorBar")
            )
            commitish.set(
                rootProject.providers.gradleProperty("publish.github.commitish")
                    .orElse(rootProject.providers.environmentVariable("GITHUB_SHA"))
                    .orElse(rootProject.providers.environmentVariable("GITHUB_REF_NAME"))
                    .orElse("main")
            )
            tagName.set(modVersion.map { "v$it" })
            allowEmptyFiles.set(true)
        }
    }

    val publishAllMods = rootProject.tasks.register("publishAllMods") {
        group = "publishing"
        description = "Publishes the GitHub release and all versioned Modrinth/CurseForge files."
        dependsOn(rootProject.tasks.named("publishMods"))
    }
    val validatePublishTargets = rootProject.tasks.register("validatePublishTargets") {
        group = "verification"
        description = "Validates versioned Modrinth/CurseForge Minecraft target metadata before uploading."
    }
    val buildAllVersionedMods = rootProject.tasks.register("buildAllVersionedMods") {
        group = "build"
        description = "Builds all versioned mod projects before any publishing task uploads files."
    }

    rootProject.gradle.projectsEvaluated {
        val targetSubprojects = rootProject.subprojects.filter { it.name == "26.2-fabric" }
        val releaseFiles = targetSubprojects
            .sortedBy { it.name }
            .map { project ->
                val loader = project.name.substringAfterLast('-')
                project.tasks.named<AbstractArchiveTask>(project.publishJarTaskName(loader)).flatMap { it.archiveFile }
            }

        rootProject.extensions.configure<ModPublishExtension>("publishMods") {
            file.set(releaseFiles.first())
            additionalFiles.from(releaseFiles.drop(1))
        }

        val subprojectBuilds = targetSubprojects.map { it.tasks.named("build") }
        val subprojectPublishTasks = targetSubprojects.map { it.tasks.named("publishMods") }

        validatePublishTargets.configure {
            doLast {
                targetSubprojects.sortedBy { it.name }.forEach { project ->
                    val targets = project.mod.prop("mc_targets")
                    val loader = project.name.substringAfterLast('-')
                    val versions = publishedMinecraftVersions(targets, project.mod.dep("minecraft.$loader"))
                    require(versions.isNotEmpty()) {
                        "${project.path}: unsupported mod.mc_targets '$targets'. Use a single version, comma-separated versions, or '>= start < end'."
                    }
                    logger.lifecycle("${project.path}: publishing for Minecraft ${versions.joinToString(", ")}")
                }
            }
        }

        buildAllVersionedMods.configure {
            dependsOn(subprojectBuilds)
            dependsOn(validatePublishTargets)
        }

        rootProject.tasks.named<Task>("publishMods") {
            mustRunAfter(buildAllVersionedMods)
        }
        subprojectPublishTasks.forEach { publishTask ->
            publishTask.configure {
                dependsOn(rootProject.tasks.named("publishMods"))
                mustRunAfter(buildAllVersionedMods)
            }
        }

        publishAllMods.configure {
            dependsOn(buildAllVersionedMods)
            dependsOn(subprojectPublishTasks)
        }
    }
}

private fun Project.publishJarTaskName(loader: String): String {
    return if (tasks.names.contains("remapJar")) {
        "remapJar"
    } else {
        "jar"
    }
}

private fun Project.providerPropertyOrEnvironment(propertyName: String, environmentName: String) =
    providers.gradleProperty(propertyName).orElse(providers.environmentVariable(environmentName))

private fun ModrinthOptions.configureMinecraftVersions(versions: List<String>) {
    versions.forEach { minecraftVersions.add(it) }
}

private fun CurseforgeOptions.configureMinecraftVersions(versions: List<String>) {
    versions.forEach { minecraftVersions.add(it) }
}

private fun publishedMinecraftVersions(range: String, fallbackVersion: String): List<String> {
    val explicitVersions = explicitMinecraftVersions(range)
    if (explicitVersions != null) {
        return explicitVersions
    }
    return minecraftVersionRange(range)
        ?.let { versionRange ->
            listOf(versionRange.start, fallbackVersion, versionRange.end.takeIf { versionRange.includeEnd })
                .filterNotNull()
                .distinct()
                .filter { compareMinecraftVersions(it, versionRange.end) <= if (versionRange.includeEnd) 0 else -1 }
        }
        ?: emptyList()
}

private fun explicitMinecraftVersions(range: String): List<String>? {
    if (range.contains('<') || range.contains('>')) {
        return null
    }
    return range.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
}

private fun minecraftVersionRange(range: String): MinecraftVersionRange? {
    val start = Regex(""">=\s*([^\s]+)""").find(range)?.groupValues?.get(1)
    val endMatch = Regex("""<(?<inclusive>=)?\s*(?<version>[^\s]+)""").find(range)
    val end = endMatch?.groups?.get("version")?.value
    val includeEnd = endMatch?.groups?.get("inclusive")?.value == "="
    return if (start != null && end != null) MinecraftVersionRange(start, end, includeEnd) else null
}

private data class MinecraftVersionRange(
    val start: String,
    val end: String,
    val includeEnd: Boolean,
)

private fun compareMinecraftVersions(left: String, right: String): Int {
    val leftParts = left.split('.').map { it.toIntOrNull() ?: 0 }
    val rightParts = right.split('.').map { it.toIntOrNull() ?: 0 }
    val maxSize = maxOf(leftParts.size, rightParts.size)
    for (index in 0 until maxSize) {
        val leftPart = leftParts.getOrElse(index) { 0 }
        val rightPart = rightParts.getOrElse(index) { 0 }
        if (leftPart != rightPart) {
            return leftPart.compareTo(rightPart)
        }
    }
    return 0
}