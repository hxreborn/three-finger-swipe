package eu.hxreborn.tfs.prefs

enum class CaptureMode(
    val key: String,
) {
    REFLECTION("reflection"),
    SYSRQ("sysrq"),
    ;

    companion object {
        fun fromKey(key: String): CaptureMode = entries.find { it.key == key } ?: REFLECTION
    }
}
