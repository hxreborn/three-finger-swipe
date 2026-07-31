package eu.hxreborn.tfs.prefs

enum class SplitMethod(
    val key: String,
) {
    NATIVE("native"),
    WM_SHELL("wm_shell"),
    ;

    companion object {
        fun fromKey(key: String): SplitMethod = entries.find { it.key == key } ?: NATIVE
    }
}
