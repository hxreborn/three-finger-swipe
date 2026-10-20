package eu.hxreborn.tfs.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import eu.hxreborn.tfs.App
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.prefs.AppPrefs
import eu.hxreborn.tfs.prefs.PrefSpec
import eu.hxreborn.tfs.util.Logger
import io.github.libxposed.service.HookedTarget
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

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

    private val _moduleStatus = MutableStateFlow<ModuleStatus>(ModuleStatus.Inactive)
    override val moduleStatus: StateFlow<ModuleStatus> = _moduleStatus.asStateFlow()

    private val statusRefreshClaim = AtomicBoolean(false)
    private val statusRefreshPending = AtomicBoolean(false)

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
        refreshModuleStatus()
    }

    override fun onServiceDied() {
        mService = null
        _moduleStatus.value = ModuleStatus.Inactive
    }

    override fun refreshModuleStatus() {
        val service = mService
        if (service == null) {
            _moduleStatus.value = ModuleStatus.Inactive
            return
        }
        statusRefreshPending.set(true)
        if (!statusRefreshClaim.compareAndSet(false, true)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                while (statusRefreshPending.getAndSet(false)) {
                    val current = mService ?: break
                    val status =
                        runCatching { readModuleStatus(current) }
                            .onFailure { Log.w(Logger.TAG, "module status read failed reason=${it.message}", it) }
                            .getOrNull()
                    if (status != null && mService === current) _moduleStatus.value = status
                }
            } finally {
                statusRefreshClaim.set(false)
            }
            if (statusRefreshPending.get() || mService !== service) refreshModuleStatus()
        }
    }

    private fun readModuleStatus(service: XposedService): ModuleStatus {
        val framework =
            Framework(
                name = service.frameworkName,
                version = service.frameworkVersion,
                apiVersion = service.apiVersion,
            )
        if (framework.apiVersion < XposedService.API_102) return ModuleStatus.Enabled(framework)
        val target =
            runCatching { service.runningTargets.firstOrNull() }
                .getOrElse { return ModuleStatus.Enabled(framework) }
        return when {
            target == null -> ModuleStatus.RebootNeeded(framework)
            target.state == HookedTarget.State.UP_TO_DATE -> ModuleStatus.Running(framework)
            else -> ModuleStatus.Outdated(framework, target.loadedVersionCode)
        }
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
                        refreshModuleStatus()
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
