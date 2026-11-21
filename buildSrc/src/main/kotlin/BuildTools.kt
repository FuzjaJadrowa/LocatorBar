import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File
import java.util.Properties

val Project.mod: ModData get() = ModData(this)

fun Project.prop(key: String): String? {
    findProperty(key)?.toString()?.let { return it }
    if (this != rootProject && name.contains('-')) {
        val version = name.substringBeforeLast('-')
        val targetPropsFile = rootProject.file("gradle/targets/$version.properties")
        if (targetPropsFile.isFile) {
            val cacheKey = "locatorbar.targetProps.$version"
            val props = if (rootProject.extensions.extraProperties.has(cacheKey)) {
                @Suppress("UNCHECKED_CAST")
                rootProject.extensions.extraProperties.get(cacheKey) as Properties
            } else {
                Properties().apply {
                    targetPropsFile.inputStream().use { load(it) }
                    rootProject.extensions.extraProperties.set(cacheKey, this)
                }
            }
            props.getProperty(key)?.let { return it }
        }
    }
    return null
}

fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) { name = alias } }
    filter { groups.forEach(::includeGroup) }
}

fun ProcessResources.properties(files: Iterable<String>, vararg properties: Pair<String, Any>) {
    for ((name, value) in properties) inputs.property(name, value)
    filesMatching(files) {
        expand(properties.toMap())
    }
}

fun Project.configureLocatorBarCommon(
    loader: String,
    isActive: Boolean,
    archiveTaskName: String = "jar",
    clientRunTask: String = "runClient",
    serverRunTask: String = "runServer"
) {
    val minecraftTitle = mod.prop("mc_title")
    val javaVersion = mod.prop("java_version")

    version = "${mod.version}+$minecraftTitle"
    group = mod.group
    extensions.configure<BasePluginExtension> {
        archivesName.set("${mod.name}-$loader")
    }

    extensions.getByType<SourceSetContainer>().apply {
        named("main") {
            resources.srcDir(rootProject.file("src/common/src/main/resources"))
            resources.srcDir(rootProject.file("src/$loader/src/main/resources"))
        }
        named("test") {
            java.setSrcDirs(listOf(rootProject.file("src/common/src/test/java")))
        }
    }

    versionedJavaSources(
        rootProject.file("src/common/src/main/java"),
        rootProject.file("src/$loader/src/main/java")
    )

    repositories.apply {
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.terraformersmc.com/")
    }

    val requiredJava = JavaVersion.toVersion(javaVersion)
    extensions.configure<JavaPluginExtension> {
        targetCompatibility = requiredJava
        sourceCompatibility = requiredJava
    }

    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.11.4")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    val buildAndCollect = tasks.register<Copy>("buildAndCollect") {
        group = "build"
        from(tasks.named<AbstractArchiveTask>(archiveTaskName).flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
        dependsOn("build")
    }

    if (isActive) {
        rootProject.tasks.register("buildActive") {
            group = "project"
            dependsOn(buildAndCollect)
        }

        rootProject.tasks.register("testClient") {
            group = "project"
            dependsOn(tasks.named(clientRunTask))
        }

        rootProject.tasks.register("testServer") {
            group = "project"
            dependsOn(tasks.named(serverRunTask))
        }
    }

    tasks.named<ProcessResources>("processResources") {
        properties(
            listOf("*.mixins.json"),
            "java" to javaVersion
        )
        doLast {
            fileTree(outputs.files.singleFile).matching {
                include("**/*.json")
            }.forEach { file ->
                file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
            }
        }
    }

    tasks.named("build") {
        group = "versioned"
        description = "Must run through 'chiseledBuild'"
    }
}

fun Project.versionedJavaSources(vararg roots: File) {
    val generatedSources = layout.buildDirectory.dir("generated/preprocessed/main")
    val prepareSources = tasks.register("prepareVersionedJavaSources") {
        inputs.files(roots)
        inputs.property("target", project.name)
        outputs.dir(generatedSources)
        dependsOn(tasks.matching { it.name == "stonecutterGenerate" })

        doLast {
            val outputRoot = generatedSources.get().asFile
            outputRoot.deleteRecursively()

            for (root in roots) {
                if (!root.exists()) {
                    continue
                }

                root.walkTopDown()
                    .filter { it.isFile && it.extension == "java" }
                    .forEach { file ->
                        val relative = root.toPath().relativize(file.toPath())
                        val output = outputRoot.toPath().resolve(relative).toFile()
                        output.parentFile.mkdirs()
                        output.writeText(Preprocessor.transform(file.readLines(), project.name.substringBeforeLast('-'), project.name.substringAfterLast('-'), file.path))
                    }
            }
        }
    }

    extensions.getByType<SourceSetContainer>().named("main") {
        if (System.getProperty("idea.sync.active") == "true") {
            java.setSrcDirs(roots.toList())
        } else {
            java.setSrcDirs(listOf(generatedSources))
        }
    }
    tasks.named("compileJava") {
        dependsOn(prepareSources)
        dependsOn(tasks.matching { it.name == "stonecutterGenerate" })
    }
}

@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = requireNotNull(project.prop("mod.id")) { "Missing 'mod.id'" }
    val name: String get() = requireNotNull(project.prop("mod.name")) { "Missing 'mod.name'" }
    val version: String get() = requireNotNull(project.prop("mod.version")) { "Missing 'mod.version'" }
    val group: String get() = requireNotNull(project.prop("mod.group")) { "Missing 'mod.group'" }

    fun prop(key: String) = requireNotNull(project.prop("mod.$key")) { "Missing 'mod.$key'" }
    fun dep(key: String) = requireNotNull(project.prop("dep.$key")) { "Missing 'dep.$key'" }
}