package eu.hxreborn.tfs.xposed.hook

import android.content.SharedPreferences
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs

// Cached snapshot of remote prefs read once per process and refreshed by the cross-process
// listener registered in ThreeFingerSwipeModule.onSystemServerStarting. Touch events in
// GestureHandler read these vars directly; calling getRemotePreferences from an interceptor
// is a Binder IPC and would block the input thread inside system_server.

@Volatile internal var debugLogs: Boolean = Prefs.DEBUG_LOGS.default

@Volatile internal var swipeThresholdPct: Int = Prefs.SWIPE_THRESHOLD_PCT.default

@Volatile internal var edgeExclusionDp: Int = Prefs.EDGE_EXCLUSION_DP.default

@Volatile internal var fingerLandingMs: Int = Prefs.FINGER_LANDING_MS.default

@Volatile internal var cooldownMs: Int = Prefs.COOLDOWN_MS.default

@Volatile internal var captureMode: CaptureMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.default)

@Volatile internal var selectedAction: ActionId = ActionId.fromKey(Prefs.SELECTED_ACTION.default)

internal fun loadHookPrefs(prefs: SharedPreferences) {
    debugLogs = Prefs.DEBUG_LOGS.read(prefs)
    swipeThresholdPct = Prefs.SWIPE_THRESHOLD_PCT.read(prefs)
    edgeExclusionDp = Prefs.EDGE_EXCLUSION_DP.read(prefs)
    fingerLandingMs = Prefs.FINGER_LANDING_MS.read(prefs)
    cooldownMs = Prefs.COOLDOWN_MS.read(prefs)
    captureMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.read(prefs))
    selectedAction = ActionId.fromKey(Prefs.SELECTED_ACTION.read(prefs))
}
