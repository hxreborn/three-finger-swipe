package eu.hxreborn.tfs.prefs

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.util.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PrefsRepository(
    private val local: SharedPreferences,
    private val remoteProvider: () -> SharedPreferences? = { null },
) {
    val state: Flow<AppPrefs> =
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
            local.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { local.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    fun <T : Any> save(
        pref: PrefSpec<T>,
        value: T,
    ) {
        local.edit { pref.write(this, value) }
        pushToRemote { pref.write(this, value) }
    }

    fun resetAll() {
        local.edit { Prefs.all.forEach { it.reset(this) } }
        pushToRemote { Prefs.all.forEach { it.reset(this) } }
    }

    fun restoreState(state: AppPrefs) {
        local.edit { writeState(state) }
        pushToRemote { writeState(state) }
    }

    private fun SharedPreferences.Editor.writeState(state: AppPrefs) {
        Prefs.SWIPE_THRESHOLD_PCT.write(this, state.swipeThresholdPct)
        Prefs.EDGE_EXCLUSION_DP.write(this, state.edgeExclusionDp)
        Prefs.FINGER_LANDING_MS.write(this, state.fingerLandingMs)
        Prefs.COOLDOWN_MS.write(this, state.cooldownMs)
        Prefs.CAPTURE_MODE.write(this, state.captureMode.key)
        Prefs.SPLIT_METHOD.write(this, state.splitMethod.key)
        Prefs.SELECTED_ACTION.write(this, state.selectedAction.key)
    }

    fun syncToRemote() {
        val remote = remoteProvider() ?: return
        runCatching {
            remote.edit { Prefs.all.forEach { it.copyIfChanged(local, remote, this) } }
        }.onFailure { Log.w(Logger.TAG, "remote prefs sync failed reason=${it.message}", it) }
    }

    private fun pushToRemote(block: SharedPreferences.Editor.() -> Unit) {
        val remote = remoteProvider() ?: return
        runCatching { remote.edit(action = block) }.onFailure {
            Log.w(Logger.TAG, "remote prefs push failed reason=${it.message}", it)
        }
    }

    private fun readState() =
        AppPrefs(
            swipeThresholdPct = Prefs.SWIPE_THRESHOLD_PCT.read(local),
            edgeExclusionDp = Prefs.EDGE_EXCLUSION_DP.read(local),
            fingerLandingMs = Prefs.FINGER_LANDING_MS.read(local),
            cooldownMs = Prefs.COOLDOWN_MS.read(local),
            captureMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.read(local)),
            splitMethod = SplitMethod.fromKey(Prefs.SPLIT_METHOD.read(local)),
            selectedAction = ActionId.fromKey(Prefs.SELECTED_ACTION.read(local)),
        )
}

data class AppPrefs(
    val swipeThresholdPct: Int = Prefs.SWIPE_THRESHOLD_PCT.default,
    val edgeExclusionDp: Int = Prefs.EDGE_EXCLUSION_DP.default,
    val fingerLandingMs: Int = Prefs.FINGER_LANDING_MS.default,
    val cooldownMs: Int = Prefs.COOLDOWN_MS.default,
    val captureMode: CaptureMode = CaptureMode.SYSTEM_API,
    val splitMethod: SplitMethod = SplitMethod.NATIVE,
    val selectedAction: ActionId = ActionId.SCREENSHOT,
)
