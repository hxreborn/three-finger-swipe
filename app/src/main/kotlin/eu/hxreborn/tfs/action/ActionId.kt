package eu.hxreborn.tfs.action

enum class ActionId(
    val key: String,
) {
    NO_ACTION("no_action"),
    SCREENSHOT("screenshot"),
    SCREEN_OFF("screen_off"),
    TOGGLE_FLASHLIGHT("toggle_flashlight"),
    RINGER_MODE("ringer_mode"),
    TOGGLE_SPLIT_SCREEN("toggle_split_screen"),
    ;

    companion object {
        fun fromKey(key: String): ActionId = entries.find { it.key == key } ?: SCREENSHOT
    }
}
