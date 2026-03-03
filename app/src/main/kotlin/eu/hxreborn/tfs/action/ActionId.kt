package eu.hxreborn.tfs.action

enum class ActionId(
    val key: String,
) {
    NO_ACTION("no_action"),
    SCREENSHOT("screenshot"),
    RECENT_APPS("recent_apps"),
    SEARCH_ASSISTANT("search_assistant"),
    VOICE_SEARCH("voice_search"),
    LAUNCH_CAMERA("launch_camera"),
    SCREEN_OFF("screen_off"),
    LAST_APP("last_app"),
    KILL_APP("kill_app"),
    MEDIA_PLAY_PAUSE("media_play_pause"),
    TOGGLE_TORCH("toggle_torch"),
    VOLUME_PANEL("volume_panel"),
    CLEAR_NOTIFICATIONS("clear_notifications"),
    NOTIFICATION_PANEL("notification_panel"),
    QS_PANEL("qs_panel"),
    RINGER_MODE("ringer_mode"),
    ;

    companion object {
        fun fromKey(key: String): ActionId = entries.find { it.key == key } ?: SCREENSHOT
    }
}
