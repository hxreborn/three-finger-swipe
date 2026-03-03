package eu.hxreborn.tfs.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.util.log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PrefsRepository(
    private val localPrefs: SharedPreferences,
) {
    @Volatile
    private var remotePrefs: SharedPreferences? = null

    fun attachRemotePrefs(prefs: SharedPreferences?) {
        remotePrefs = prefs
        if (prefs != null) syncLocalToRemote()
    }

    val state: Flow<PrefsState> =
        callbackFlow {
            fun sendState() = trySend(readState())
            sendState()
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener {
                    _,
                    _,
                    ->
                    sendState()
                }
            localPrefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { localPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    fun <T : Any> save(
        pref: PrefSpec<T>,
        value: T,
    ) {
        localPrefs.edit { pref.write(this, value) }
        pushToRemote { pref.write(this, value) }
    }

    fun resetAll() {
        localPrefs.edit { Prefs.all.forEach { it.reset(this) } }
        pushToRemote { Prefs.all.forEach { it.reset(this) } }
    }

    fun restoreState(state: PrefsState) {
        localPrefs.edit {
            Prefs.SWIPE_ENABLED.write(this, state.swipeEnabled)
            Prefs.DEBUG_LOGS.write(this, state.debugLogs)
            Prefs.SWIPE_THRESHOLD_PCT.write(this, state.swipeThresholdPct)
            Prefs.EDGE_EXCLUSION_DP.write(this, state.edgeExclusionDp)
            Prefs.FINGER_LANDING_MS.write(this, state.fingerLandingMs)
            Prefs.COOLDOWN_MS.write(this, state.cooldownMs)
            Prefs.CAPTURE_MODE.write(this, state.captureMode.key)
            Prefs.SELECTED_ACTION.write(this, state.selectedAction.key)
        }
        pushToRemote {
            Prefs.SWIPE_ENABLED.write(this, state.swipeEnabled)
            Prefs.DEBUG_LOGS.write(this, state.debugLogs)
            Prefs.SWIPE_THRESHOLD_PCT.write(this, state.swipeThresholdPct)
            Prefs.EDGE_EXCLUSION_DP.write(this, state.edgeExclusionDp)
            Prefs.FINGER_LANDING_MS.write(this, state.fingerLandingMs)
            Prefs.COOLDOWN_MS.write(this, state.cooldownMs)
            Prefs.CAPTURE_MODE.write(this, state.captureMode.key)
            Prefs.SELECTED_ACTION.write(this, state.selectedAction.key)
        }
    }

    private fun pushToRemote(block: SharedPreferences.Editor.() -> Unit) {
        remotePrefs?.let { remote ->
            runCatching { remote.edit(action = block) }.onFailure {
                log(
                    "Failed to push remote prefs",
                    it,
                )
            }
        }
    }

    private fun syncLocalToRemote() {
        val remote = remotePrefs ?: return
        runCatching {
            remote.edit { Prefs.all.forEach { it.copyTo(localPrefs, this) } }
        }.onFailure { log("Failed to sync local prefs to remote", it) }
    }

    private fun readState() =
        PrefsState(
            swipeEnabled = Prefs.SWIPE_ENABLED.read(localPrefs),
            debugLogs = Prefs.DEBUG_LOGS.read(localPrefs),
            swipeThresholdPct = Prefs.SWIPE_THRESHOLD_PCT.read(localPrefs),
            edgeExclusionDp = Prefs.EDGE_EXCLUSION_DP.read(localPrefs),
            fingerLandingMs = Prefs.FINGER_LANDING_MS.read(localPrefs),
            cooldownMs = Prefs.COOLDOWN_MS.read(localPrefs),
            captureMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.read(localPrefs)),
            selectedAction = ActionId.fromKey(Prefs.SELECTED_ACTION.read(localPrefs)),
        )
}

data class PrefsState(
    val swipeEnabled: Boolean = Prefs.SWIPE_ENABLED.default,
    val debugLogs: Boolean = Prefs.DEBUG_LOGS.default,
    val swipeThresholdPct: Int = Prefs.SWIPE_THRESHOLD_PCT.default,
    val edgeExclusionDp: Int = Prefs.EDGE_EXCLUSION_DP.default,
    val fingerLandingMs: Int = Prefs.FINGER_LANDING_MS.default,
    val cooldownMs: Int = Prefs.COOLDOWN_MS.default,
    val captureMode: CaptureMode = CaptureMode.REFLECTION,
    val selectedAction: ActionId = ActionId.SCREENSHOT,
)
