package eu.hxreborn.tfs.ui.viewmodel

import androidx.lifecycle.ViewModel
import eu.hxreborn.tfs.prefs.AppPrefs
import eu.hxreborn.tfs.prefs.PrefSpec
import kotlinx.coroutines.flow.StateFlow

abstract class SettingsViewModel : ViewModel() {
    abstract val uiState: StateFlow<AppPrefs>
    abstract val pendingReboot: StateFlow<Boolean>
    abstract val xposedActive: StateFlow<Boolean>

    abstract fun setXposedActive(active: Boolean)

    abstract fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    )

    abstract fun resetToDefaults()

    abstract fun restoreState(state: AppPrefs)
}
