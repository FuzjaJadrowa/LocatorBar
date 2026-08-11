plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.1-fabric"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge")
}