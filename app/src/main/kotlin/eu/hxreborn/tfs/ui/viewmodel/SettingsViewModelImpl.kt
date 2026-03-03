package eu.hxreborn.tfs.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import eu.hxreborn.tfs.prefs.PrefSpec
import eu.hxreborn.tfs.prefs.PrefsRepository
import eu.hxreborn.tfs.prefs.PrefsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _pendingReboot = MutableStateFlow(false)
    override val pendingReboot: StateFlow<Boolean> = _pendingReboot.asStateFlow()

    private val _xposedActive = MutableStateFlow(false)
    override val xposedActive: StateFlow<Boolean> = _xposedActive.asStateFlow()

    override fun setXposedActive(active: Boolean) {
        _xposedActive.value = active
    }

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
        repository.save(pref, value)
        _pendingReboot.value = true
    }

    override fun resetToDefaults() {
        repository.resetAll()
        _pendingReboot.value = true
    }

    override fun restoreState(state: PrefsState) {
        repository.restoreState(state)
        _pendingReboot.value = true
    }
}

class SettingsViewModelFactory(
    private val repository: PrefsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SettingsViewModelImpl::class.java))
        return SettingsViewModelImpl(repository) as T
    }
}
