package eu.hxreborn.tfs.xposed.hook

import android.content.SharedPreferences
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.prefs.AppFilterMode
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.SplitMethod

// process-local values keep RemotePreferences Binder IPC off the input thread

@Volatile internal var swipeThresholdPct: Int = Prefs.SWIPE_THRESHOLD_PCT.default

@Volatile internal var edgeExclusionDp: Int = Prefs.EDGE_EXCLUSION_DP.default

@Volatile internal var fingerLandingMs: Int = Prefs.FINGER_LANDING_MS.default

@Volatile internal var cooldownMs: Int = Prefs.COOLDOWN_MS.default

@Volatile internal var captureMode: CaptureMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.default)

@Volatile internal var splitMethod: SplitMethod = SplitMethod.fromKey(Prefs.SPLIT_METHOD.default)

@Volatile internal var selectedAction: ActionId = ActionId.fromKey(Prefs.SELECTED_ACTION.default)

@Volatile internal var appFilterMode: AppFilterMode =
    AppFilterMode.fromKey(
        Prefs.APP_FILTER_MODE.default,
    )

@Volatile internal var filteredApps: Set<String> = Prefs.FILTERED_APPS.default

internal fun isAppFiltered(packageName: String?): Boolean {
    if (packageName == null || filteredApps.isEmpty()) return false
    return when (appFilterMode) {
        AppFilterMode.BLOCK -> packageName in filteredApps
        AppFilterMode.ALLOW -> packageName !in filteredApps
    }
}

internal fun loadHookPrefs(prefs: SharedPreferences) {
    swipeThresholdPct = Prefs.SWIPE_THRESHOLD_PCT.read(prefs)
    edgeExclusionDp = Prefs.EDGE_EXCLUSION_DP.read(prefs)
    fingerLandingMs = Prefs.FINGER_LANDING_MS.read(prefs)
    cooldownMs = Prefs.COOLDOWN_MS.read(prefs)
    captureMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.read(prefs))
    splitMethod = SplitMethod.fromKey(Prefs.SPLIT_METHOD.read(prefs))
    selectedAction = ActionId.fromKey(Prefs.SELECTED_ACTION.read(prefs))
    appFilterMode = AppFilterMode.fromKey(Prefs.APP_FILTER_MODE.read(prefs))
    filteredApps = Prefs.FILTERED_APPS.read(prefs)
}
