import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

val Project.mod: ModData get() = ModData(this)
fun Project.prop(key: String): String? = findProperty(key)?.toString()
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

fun Project.versionedJavaSources(vararg roots: File) {
    val generatedSources = layout.buildDirectory.dir("generated/preprocessed/main")
    val prepareSources = tasks.register("prepareVersionedJavaSources") {
        inputs.files(roots)
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
                        output.writeText(Preprocessor.transform(file.readLines(), project.name.substringBeforeLast('-')))
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