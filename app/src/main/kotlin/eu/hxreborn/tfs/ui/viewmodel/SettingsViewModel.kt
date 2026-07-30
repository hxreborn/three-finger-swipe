package eu.hxreborn.tfs.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import eu.hxreborn.tfs.prefs.AppPrefs
import eu.hxreborn.tfs.prefs.PrefSpec
import io.github.libxposed.service.XposedService
import kotlinx.coroutines.flow.StateFlow

abstract class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    abstract val uiState: StateFlow<AppPrefs>
    abstract val xposedServiceAvailable: StateFlow<Boolean>

    abstract fun onServiceBound(service: XposedService)

    abstract fun onServiceDied()

    abstract fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    )

    abstract fun resetToDefaults()

    abstract fun restoreState(state: AppPrefs)

    abstract fun triggerHotReload(onStatus: (String) -> Unit)
}
