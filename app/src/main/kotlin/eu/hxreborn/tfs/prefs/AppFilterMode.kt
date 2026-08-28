package eu.hxreborn.tfs.prefs

enum class AppFilterMode(
    val key: String,
) {
    BLOCK("block"),
    ALLOW("allow"),
    ;

    companion object {
        fun fromKey(key: String): AppFilterMode = entries.find { it.key == key } ?: BLOCK
    }
}
