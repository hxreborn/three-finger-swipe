package eu.hxreborn.tfs.ui.viewmodel

import androidx.lifecycle.ViewModel
import eu.hxreborn.tfs.prefs.PrefsState
import kotlinx.coroutines.flow.StateFlow

abstract class SettingsViewModel : ViewModel() {
    abstract val uiState: StateFlow<PrefsState>

    abstract fun setSwipeEnabled(value: Boolean)

    abstract fun setDebugLogs(value: Boolean)
}
