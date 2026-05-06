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
    }
}