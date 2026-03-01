package eu.hxreborn.tfs.prefs

import eu.hxreborn.tfs.ModuleConstants

object Prefs {
    val GROUP: String = ModuleConstants.prefsGroup

    val SWIPE_ENABLED = BoolPref("swipe_enabled", true)
    val DEBUG_LOGS = BoolPref("debug_logs", false)

    val all: List<PrefSpec<*>> =
        listOf(
            SWIPE_ENABLED,
            DEBUG_LOGS,
        )
}
