package eu.hxreborn.tfs.gesture

import eu.hxreborn.tfs.prefs.Prefs

data class GestureConfig(
    val requiredFingers: Int = 3,
    val fingerLandingWindowMs: Long = Prefs.FINGER_LANDING_MS.default.toLong(),
    val startingProximityPx: Float = 500f,
    val swipeThresholdFraction: Float = Prefs.SWIPE_THRESHOLD_PCT.default / 100f,
    val edgeExclusionDp: Float = Prefs.EDGE_EXCLUSION_DP.default.toFloat(),
    val cooldownMs: Long = Prefs.COOLDOWN_MS.default.toLong(),
)
