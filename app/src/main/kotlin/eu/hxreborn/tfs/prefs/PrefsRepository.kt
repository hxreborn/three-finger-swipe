package eu.hxreborn.tfs.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
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
        remotePrefs?.let { remote ->
            runCatching {
                remote.edit {
                    pref.write(
                        this,
                        value,
                    )
                }
            }.onFailure { log("Failed to push remote pref ${pref.key}", it) }
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
        )
}

data class PrefsState(
    val swipeEnabled: Boolean = Prefs.SWIPE_ENABLED.default,
    val debugLogs: Boolean = Prefs.DEBUG_LOGS.default,
)
