import java.util.*

object Preprocessor {
    private data class StonecutterBlock(
        val parentActive: Boolean,
        var matched: Boolean,
        var active: Boolean,
        var hasElse: Boolean = false
    )

    fun transform(lines: List<String>, version: String, loader: String, source: String = "<source>"): String {
        val output = mutableListOf<String>()
        val blocks = ArrayDeque<StonecutterBlock>()
        var nextLineActive: Boolean? = null

        fun currentActive() = blocks.all { it.active }
        var legacyComment = false
        var afterDirective = false

        for ((index, line) in lines.withIndex()) {
            fun validate(valid: Boolean, message: String) {
                require(valid) { "$source:${index + 1}: $message" }
            }
            fun condition(value: String): Boolean = try {
                evalVersion(version, loader, value)
            } catch (exception: IllegalStateException) {
                error("$source:${index + 1}: ${exception.message}")
            }
            val directive = parseDirective(line)
            if (directive != null) {
                if (line.trim().startsWith("*/")) legacyComment = false
                validate(nextLineActive == null, "Expected a source line after single-line if")
                if (directive.startsWith("}") || directive.startsWith("else") || directive.startsWith("elif")) {
                    validate(blocks.isNotEmpty(), "Directive without an open if: $directive")
                }
                when {
                    directive == "}" -> {
                        blocks.removeLast()
                    }
                    directive.startsWith("} elif ") && directive.endsWith(" {") -> {
                        val block = blocks.last()
                        validate(!block.hasElse, "elif after else")
                        val condition = directive.removePrefix("} elif ").removeSuffix(" {").trim()
                        val matches = condition(condition)
                        val active = block.parentActive && !block.matched && matches
                        block.active = active
                        block.matched = block.matched || active
                    }
                    directive == "} else {" -> {
                        val block = blocks.last()
                        validate(!block.hasElse, "Duplicate else")
                        block.hasElse = true
                        val active = block.parentActive && !block.matched
                        block.active = active
                        block.matched = true
                    }
                    directive.startsWith("if ") && directive.endsWith(" {") -> {
                        val condition = directive.removePrefix("if ").removeSuffix(" {").trim()
                        val matches = condition(condition)
                        val active = currentActive() && matches
                        blocks.addLast(StonecutterBlock(currentActive(), active, active))
                    }
                    directive.startsWith("elif ") && directive.endsWith(" {") -> {
                        val block = blocks.last()
                        validate(!block.hasElse, "elif after else")
                        val condition = directive.removePrefix("elif ").removeSuffix(" {").trim()
                        val matches = condition(condition)
                        val active = block.parentActive && !block.matched && matches
                        block.active = active
                        block.matched = block.matched || active
                    }
                    directive == "else {" -> {
                        val block = blocks.last()
                        validate(!block.hasElse, "Duplicate else")
                        block.hasElse = true
                        val active = block.parentActive && !block.matched
                        block.active = active
                        block.matched = true
                    }
                    directive.startsWith("if ") -> {
                        val condition = directive.removePrefix("if ").trim()
                        val matches = condition(condition)
                        nextLineActive = currentActive() && matches
                    }
                    else -> validate(false, "Unsupported directive: $directive")
                }
                afterDirective = directive != "}"
                continue
            }

            var sourceLine = line
            if (afterDirective && line.trimStart().startsWith("/*") && !line.trimStart().startsWith("/**")) {
                sourceLine = sourceLine.replaceFirst("/*", "")
                legacyComment = true
            }
            if (legacyComment && sourceLine.trimEnd().endsWith("*/")) {
                sourceLine = sourceLine.substringBeforeLast("*/")
                legacyComment = false
            }
            if (line.isNotBlank()) afterDirective = false
            val lineActive = nextLineActive ?: currentActive()
            nextLineActive = null
            if (!lineActive) {
                continue
            }

            output += applyReplacements(sourceLine, version, loader)
        }

        require(blocks.isEmpty()) { "$source:${lines.size}: Unclosed if block" }
        require(nextLineActive == null) { "$source:${lines.size}: Missing source line after if" }
        require(!legacyComment) { "$source:${lines.size}: Unclosed legacy comment wrapper" }
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

    private fun applyReplacements(line: String, version: String, loader: String): String {
        var result = line
        if (evalVersion(version, loader, "<1.21")) {
            result = result
                .replace("Identifier.fromNamespaceAndPath(", "new Identifier(")
        }
        if (evalVersion(version, loader, "<1.21.11")) {
            result = result
                .replace("Identifier", "ResourceLocation")
                .replace("import net.minecraft.world.entity.player.PlayerSkin;", "import net.minecraft.client.resources.PlayerSkin;")
                .replace(".identifier()", ".location()")
        }
        if (evalVersion(version, loader, "<26.1")) {
            result = result
                .replace("GuiGraphicsExtractor", "GuiGraphics")
                .replace(".text(", ".drawString(")
                .replace(".centeredText(", ".drawCenteredString(")
                .replace("RenderCompat.drawString(", "RenderCompat.text(")
        }
        return result
    }

    private fun evalVersion(version: String, loader: String, condition: String): Boolean {
        if (condition == "fabric") return loader == "fabric"
        if (condition == "!fabric") return loader != "fabric"
        if (condition == "neoforge") return loader == "neoforge"
        if (condition == "!neoforge") return loader != "neoforge"
        if (condition == "forge") return loader == "forge"
        if (condition == "!forge") return loader != "forge"

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
        val numericVersion = Regex("[0-9]+(?:\\.[0-9]+)*")
        check(numericVersion.matches(left) && numericVersion.matches(right)) { "Invalid version comparison: $left, $right" }
        val leftParts = left.split('.').map { it.toIntOrNull() ?: error("Invalid version: $left") }
        val rightParts = right.split('.').map { it.toIntOrNull() ?: error("Invalid version: $right") }
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