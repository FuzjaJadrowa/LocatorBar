import java.io.File
import javax.tools.ToolProvider
import com.sun.source.util.JavacTask
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import kotlin.test.*

class PreprocessorTest {
    private fun transform(source: String, version: String = "1.20.1", loader: String = "forge") =
        Preprocessor.transform(source.trimIndent().lines(), version, loader).trim()

    @Test fun branchesAndLegacyComments() {
        val source = """
            //? if >=26.1 {
            modern
            //?} elif >=1.20.5 {
            /*components*/
            //?} else {
            /*legacy
            nbt*/
            //?}
        """
        assertEquals("legacy\nnbt", transform(source).replace("\r", ""))
        assertEquals("components", transform(source, "1.21.1"))
        assertEquals("modern", transform(source, "26.3"))
        assertEquals("yes", transform("//? if forge\nyes\n//? if fabric\nno"))
        assertEquals("", transform("//? if fabric {\nno\n//?}"))
        assertEquals("yes", transform("//? if !fabric {\nyes\n//?} else {\nno\n//?}"))
    }

    @Test fun nestedConditionsAndNumericVersions() {
        val source = """
            //? if >=1.21.2 {
            //? if neoforge {
            yes
            //?} else {
            other
            //?}
            //?} else {
            old
            //?}
        """
        assertEquals("yes", transform(source, "1.21.11", "neoforge"))
        assertEquals("other", transform(source, "26.3", "fabric"))
        assertEquals("old", transform(source))
        for (operator in listOf("==", ">=", "<=")) {
            assertEquals("yes", transform("//? if $operator 1.20.1.0\nyes"))
        }
        assertEquals("yes", transform("//? if >1.20\nyes"))
        assertEquals("yes", transform("//? if <1.21\nyes"))
    }

    @Test fun malformedDirectivesFailIncludingInactiveBranches() {
        for (source in listOf(
            "//?}", "//?} else {", "//?elif >=1.21 {", "//? if forge {",
            "//? if forge", "//? unknown", "//? if forge\n//? if fabric\nx",
            "//? if forge {\n//?} else {\n//?} else {\n//?}",
            "//? if forge {\n//?} else {\n//?} elif fabric {\n//?}",
            "//? if fabric {\n//? if >=garbage {\n//?}\n//?}",
            "//? if >=1.bad.2\nx", "//? if >=1.21 extra\nx", "//? if >=999999999999999\nx",
            "//? if forge {\n/*legacy\n//?}"
        )) {
            val error = assertFails { transform(source) }
            assertContains(error.message.orEmpty(), "<source>:")
        }
    }

    @Test fun ordinaryCommentsAndReplacements() {
        assertEquals("/** docs */\n/* ordinary */\nString s = \"/* text */\";",
            transform("/** docs */\n/* ordinary */\nString s = \"/* text */\";").replace("\r", ""))
        assertEquals("new ResourceLocation(\"x\", \"y\")", transform("Identifier.fromNamespaceAndPath(\"x\", \"y\")"))
        assertEquals("GuiGraphics g; g.drawString(f); RenderCompat.text(g);",
            transform("GuiGraphicsExtractor g; g.text(f); RenderCompat.text(g);"))
        assertEquals("/* ordinary */", transform("//? if forge {\n//?}\n/* ordinary */"))
    }

    @Test fun allRepositoryTargetsGenerateSyntacticallyValidJava() {
        val root = File(System.getProperty("locatorbar.root"))
        val versions = listOf("1.20.1-forge", "1.21.1-fabric", "1.21.1-neoforge", "1.21.11-fabric",
            "1.21.11-neoforge", "26.1.2-fabric", "26.1.2-neoforge", "26.3-fabric", "26.3-neoforge")
        val compiler = ToolProvider.getSystemJavaCompiler()
        for (target in versions) {
            val version = target.substringBeforeLast('-')
            val loader = target.substringAfterLast('-')
            val generated = mutableListOf<File>()
            for (sourceRoot in listOf("common", loader)) {
                File(root, "src/$sourceRoot/src/main/java").walkTopDown().filter { it.extension == "java" }.forEach { file ->
                    val result = Preprocessor.transform(file.readLines(), version, loader, file.path)
                    val output = File(root, "buildSrc/build/source-audit/$target/${file.name}")
                    output.parentFile.mkdirs()
                    output.writeText(result)
                    generated.add(output)
                    if (file.name == "LocatorBarUtils.java" && target == "1.20.1-forge") {
                        assertContains(result, "ItemStack.of(itemTag)")
                        assertFalse(result.contains("DataComponents"))
                    }
                    if (file.name in listOf("LocatorBarUtils.java", "WaypointInventory.java")) {
                        when {
                            version == "26.3" -> {
                                assertContains(result, "bundleContents.itemCopies()")
                                assertFalse(result.contains("itemCopyStream()"))
                            }
                            version != "1.20.1" -> {
                                assertContains(result, "bundleContents.itemCopyStream()")
                                assertFalse(result.contains("itemCopies()"))
                            }
                        }
                    }
                }
            }
            val diagnostics = DiagnosticCollector<JavaFileObject>()
            compiler.getStandardFileManager(diagnostics, null, null).use { manager ->
                val task = compiler.getTask(null, manager, diagnostics, listOf("-proc:none"), null,
                    manager.getJavaFileObjectsFromFiles(generated)) as JavacTask
                task.parse().toList()
                assertTrue(diagnostics.diagnostics.none { it.kind == javax.tools.Diagnostic.Kind.ERROR },
                    "$target: ${diagnostics.diagnostics.joinToString("\n")}")
            }
        }
    }
}
