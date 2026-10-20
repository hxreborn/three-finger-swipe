package eu.hxreborn.tfs.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import eu.hxreborn.tfs.prefs.AppPrefs
import eu.hxreborn.tfs.prefs.PrefSpec
import io.github.libxposed.service.XposedService
import kotlinx.coroutines.flow.StateFlow

@Immutable
data class Framework(
    val name: String,
    val version: String,
    val apiVersion: Int,
)

@Immutable
sealed interface ModuleStatus {
    data object Inactive : ModuleStatus

    data class Enabled(
        val framework: Framework,
    ) : ModuleStatus

    data class RebootNeeded(
        val framework: Framework,
    ) : ModuleStatus

    data class Outdated(
        val framework: Framework,
        val loadedVersionCode: Long,
    ) : ModuleStatus

    data class Running(
        val framework: Framework,
    ) : ModuleStatus
}

val ModuleStatus.framework: Framework?
    get() =
        when (this) {
            ModuleStatus.Inactive -> null
            is ModuleStatus.Enabled -> framework
            is ModuleStatus.RebootNeeded -> framework
            is ModuleStatus.Outdated -> framework
            is ModuleStatus.Running -> framework
        }

abstract class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    abstract val uiState: StateFlow<AppPrefs>
    abstract val moduleStatus: StateFlow<ModuleStatus>

    abstract fun onServiceBound(service: XposedService)

    abstract fun onServiceDied()

    abstract fun refreshModuleStatus()

    abstract fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    )

    abstract fun resetToDefaults()

    abstract fun restoreState(state: AppPrefs)

    abstract fun triggerHotReload(onStatus: (String) -> Unit)
}
