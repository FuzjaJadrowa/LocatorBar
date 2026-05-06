import org.gradle.kotlin.dsl.replace

plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "26.1-fabric"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"

    replacements {
        string {
            direction = eval(current.version, ">=1.21.11")
            replace("ResourceLocation", "Identifier")
        }
        string {
            direction = eval(current.version, "<1.21.11")
            replace("Identifier", "ResourceLocation")
        }
        string {
            direction = eval(current.version, "<1.21.11")
            replace("import net.minecraft.world.entity.player.PlayerSkin;", "import net.minecraft.client.resources.PlayerSkin;")
        }
        string {
            direction = eval(current.version, "<1.21.11")
            replace(".identifier()", ".location()")
        }
        string {
            direction = eval(current.version, "<26.1")
            replace("GuiGraphicsExtractor", "GuiGraphics")
        }
        string {
            direction = eval(current.version, "<26.1")
            replace(".text(", ".drawString(")
        }
        string {
            direction = eval(current.version, "<26.1")
            replace(".centeredText(", ".drawCenteredString(")
        }
    }
}
