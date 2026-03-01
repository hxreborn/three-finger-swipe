package eu.hxreborn.tfs.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.PrefsRepository
import eu.hxreborn.tfs.prefs.PrefsState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModelImpl(
    private val repository: PrefsRepository,
) : SettingsViewModel() {
    override val uiState: StateFlow<PrefsState> =
        repository.state.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PrefsState(),
        )

    override fun setSwipeEnabled(value: Boolean) = repository.save(Prefs.SWIPE_ENABLED, value)

    override fun setDebugLogs(value: Boolean) = repository.save(Prefs.DEBUG_LOGS, value)
}

class SettingsViewModelFactory(
    private val repository: PrefsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModelImpl(repository) as T
}
