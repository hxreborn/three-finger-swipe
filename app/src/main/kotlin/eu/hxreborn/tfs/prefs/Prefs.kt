package eu.hxreborn.tfs.prefs

import eu.hxreborn.tfs.ModuleConstants

object Prefs {
    val GROUP: String = ModuleConstants.prefsGroup

    val SWIPE_ENABLED = BoolPref("swipe_enabled", true)
    val DEBUG_LOGS = BoolPref("debug_logs", false)

    val SWIPE_THRESHOLD_PCT = IntPref("swipe_threshold_pct", 14, 5..30)
    val EDGE_EXCLUSION_DP = IntPref("edge_exclusion_dp", 50, 0..150)
    val FINGER_LANDING_MS = IntPref("finger_landing_ms", 800, 200..1500)
    val COOLDOWN_MS = IntPref("cooldown_ms", 500, 100..2000)
    val CAPTURE_MODE = StringPref("capture_mode", CaptureMode.REFLECTION.key)

    val all: List<PrefSpec<*>> =
        listOf(
            SWIPE_ENABLED,
            DEBUG_LOGS,
            SWIPE_THRESHOLD_PCT,
            EDGE_EXCLUSION_DP,
            FINGER_LANDING_MS,
            COOLDOWN_MS,
            CAPTURE_MODE,
        )
}
