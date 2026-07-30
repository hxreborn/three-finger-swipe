package eu.hxreborn.tfs.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import eu.hxreborn.tfs.App
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.prefs.AppPrefs
import eu.hxreborn.tfs.prefs.PrefSpec
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModelImpl(
    application: Application,
) : SettingsViewModel(application) {
    private val app = App.from(application)

    @Volatile
    private var mService: XposedService? = null

    override val uiState: StateFlow<AppPrefs> =
        app.prefs.state.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppPrefs(),
        )

    private val _xposedServiceAvailable = MutableStateFlow(false)
    override val xposedServiceAvailable: StateFlow<Boolean> =
        _xposedServiceAvailable.asStateFlow()

    private val serviceListener =
        object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                onServiceBound(service)
            }

            override fun onServiceDied(service: XposedService) {
                onServiceDied()
            }
        }

    init {
        app.addServiceListener(serviceListener)
    }

    override fun onCleared() {
        super.onCleared()
        app.removeServiceListener(serviceListener)
    }

    override fun onServiceBound(service: XposedService) {
        mService = service
        _xposedServiceAvailable.value = true
    }

    override fun onServiceDied() {
        mService = null
        _xposedServiceAvailable.value = false
    }

    override fun <T : Any> savePref(
        pref: PrefSpec<T>,
        value: T,
    ) {
        app.prefs.save(pref, value)
    }

    override fun resetToDefaults() {
        app.prefs.resetAll()
    }

    override fun restoreState(state: AppPrefs) {
        app.prefs.restoreState(state)
    }

    override fun triggerHotReload(onStatus: (String) -> Unit) {
        if (!BuildConfig.DEBUG) return

        fun report(message: String) {
            viewModelScope.launch(Dispatchers.Main) { onStatus(message) }
        }

        val service = mService
        if (service == null) {
            report("hot reload: service not bound")
            return
        }
        if (service.apiVersion < XposedService.API_102) {
            report("hot reload needs api 102, framework is ${service.apiVersion}")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val targets = service.getRunningTargets()
                if (targets.isEmpty()) {
                    report("hot reload: no running targets")
                    return@runCatching
                }
                targets.forEach { target ->
                    service.hotReloadModule(target, null) { reloaded, result ->
                        report("hot reload ${reloaded.processName}: ${result.status()}")
                    }
                }
            }.onFailure { report("hot reload failed: ${it.message}") }
        }
    }

    companion object {
        val Factory =
            viewModelFactory {
                initializer<SettingsViewModel> {
                    SettingsViewModelImpl(
                        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application,
                    )
                }
            }
    }
}
