import java.util.*

object Preprocessor {
    private data class StonecutterBlock(
        val parentActive: Boolean,
        var matched: Boolean,
        var active: Boolean
    )

    fun transform(lines: List<String>, version: String): String {
        val output = mutableListOf<String>()
        val blocks = ArrayDeque<StonecutterBlock>()
        var nextLineActive: Boolean? = null

        fun currentActive() = blocks.all { it.active }
        fun parentActive() = blocks.toList().dropLast(1).all { it.active }

        for (line in lines) {
            val directive = parseDirective(line)
            if (directive != null) {
                when {
                    directive == "}" -> {
                        blocks.removeLast()
                    }
                    directive.startsWith("} elif ") && directive.endsWith(" {") -> {
                        val block = blocks.last()
                        val condition = directive.removePrefix("} elif ").removeSuffix(" {").trim()
                        val active = block.parentActive && !block.matched && evalVersion(version, condition)
                        block.active = active
                        block.matched = block.matched || active
                    }
                    directive == "} else {" -> {
                        val block = blocks.last()
                        val active = block.parentActive && !block.matched
                        block.active = active
                        block.matched = true
                    }
                    directive.startsWith("if ") && directive.endsWith(" {") -> {
                        val condition = directive.removePrefix("if ").removeSuffix(" {").trim()
                        val active = currentActive() && evalVersion(version, condition)
                        blocks.addLast(StonecutterBlock(currentActive(), active, active))
                    }
                    directive.startsWith("elif ") && directive.endsWith(" {") -> {
                        val block = blocks.last()
                        val condition = directive.removePrefix("elif ").removeSuffix(" {").trim()
                        val active = block.parentActive && !block.matched && evalVersion(version, condition)
                        block.active = active
                        block.matched = block.matched || active
                    }
                    directive == "else {" -> {
                        val block = blocks.last()
                        val active = block.parentActive && !block.matched
                        block.active = active
                        block.matched = true
                    }
                    directive.startsWith("if ") -> {
                        val condition = directive.removePrefix("if ").trim()
                        nextLineActive = currentActive() && evalVersion(version, condition)
                    }
                }
                continue
            }

            val lineActive = nextLineActive ?: currentActive()
            nextLineActive = null
            if (!lineActive) {
                continue
            }

            output += applyReplacements(uncommentLine(line), version)
        }

        return output.joinToString(System.lineSeparator(), postfix = System.lineSeparator())
    }

    private fun parseDirective(line: String): String? {
        val trimmed = line.trim()
        return when {
            trimmed.startsWith("//?") -> trimmed.removePrefix("//?").trim()
            trimmed.startsWith("*///?") -> trimmed.removePrefix("*///?").trim()
            else -> null
        }
    }

    private fun uncommentLine(line: String): String {
        var result = line
        val open = result.indexOf("/*")
        if (open >= 0) {
            result = result.removeRange(open, open + 2)
        }
        val close = result.indexOf("*/")
        if (close >= 0) {
            result = result.removeRange(close, close + 2)
        }
        return result
    }

    private fun applyReplacements(line: String, version: String): String {
        var result = line
        if (evalVersion(version, "<1.21.11")) {
            result = result
                .replace("Identifier", "ResourceLocation")
                .replace("import net.minecraft.world.entity.player.PlayerSkin;", "import net.minecraft.client.resources.PlayerSkin;")
                .replace(".identifier()", ".location()")
        }
        if (evalVersion(version, "<26.1")) {
            result = result
                .replace("GuiGraphicsExtractor", "GuiGraphics")
                .replace(".text(", ".drawString(")
                .replace(".centeredText(", ".drawCenteredString(")
                .replace("RenderCompat.drawString(", "RenderCompat.text(")
        }
        return result
    }

    private fun evalVersion(version: String, condition: String): Boolean {
        val parts = condition.trim().split(Regex("\\s+"), limit = 2)
        val operator: String
        val compared: String
        if (parts.size == 1) {
            val match = Regex("(>=|<=|>|<|==)(.+)").matchEntire(parts[0])
                ?: error("Unsupported Stonecutter condition: $condition")
            operator = match.groupValues[1]
            compared = match.groupValues[2]
        } else {
            operator = parts[0]
            compared = parts[1]
        }

        val comparison = compareVersions(version, compared)
        return when (operator) {
            ">=" -> comparison >= 0
            ">" -> comparison > 0
            "<=" -> comparison <= 0
            "<" -> comparison < 0
            "==" -> comparison == 0
            else -> error("Unsupported Stonecutter operator: $operator")
        }
    }

    private fun compareVersions(left: String, right: String): Int {
        val leftParts = left.split('.', '-').mapNotNull { it.toIntOrNull() }
        val rightParts = right.split('.', '-').mapNotNull { it.toIntOrNull() }
        val size = maxOf(leftParts.size, rightParts.size)

        for (index in 0 until size) {
            val leftValue = leftParts.getOrElse(index) { 0 }
            val rightValue = rightParts.getOrElse(index) { 0 }
            if (leftValue != rightValue) {
                return leftValue.compareTo(rightValue)
            }
        }

        return 0
    }
}