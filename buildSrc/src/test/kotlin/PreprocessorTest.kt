import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreprocessorTest {
    @Test
    fun `evaluates version comparisons and logical operators`() {
        assertTrue(Preprocessor.evalVersion("1.21.11", "fabric", ">=1.21.11 && <26.2 && fabric"))
        assertFalse(Preprocessor.evalVersion("1.21.11", "neoforge", ">=1.21.11 && <26.2 && fabric"))
        assertTrue(Preprocessor.evalVersion("1.21.1", "fabric", "!fabric || <1.21.11"))
        assertFalse(Preprocessor.evalVersion("1.21.11", "fabric", "!fabric || <1.21.11"))
        assertTrue(Preprocessor.evalVersion("26.1.2", "neoforge", ">=26.1 && neoforge"))
    }

    @Test
    fun `transforms conditional blocks and replacements`() {
        val lines = listOf(
            "//? if >=26.1 && fabric {",
            "GuiGraphicsExtractor g;",
            "//?} else {",
            "/*GuiGraphicsExtractor fallback;",
            "*///?}"
        )

        val modernFabric = Preprocessor.transform(lines, "26.1.2", "fabric")
        assertEquals("GuiGraphicsExtractor g;" + System.lineSeparator(), modernFabric)

        val olderNeoForge = Preprocessor.transform(lines, "1.21.1", "neoforge")
        assertEquals("GuiGraphics fallback;" + System.lineSeparator(), olderNeoForge)
    }
}