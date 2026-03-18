package eu.hxreborn.tfs.xposed

import android.util.Log
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.ModuleConstants
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

class ThreeFingerSwipeModule : XposedModule() {
    override fun onModuleLoaded(param: ModuleLoadedParam) {
        Logger.attach(this)
        log(
            Log.INFO,
            ModuleConstants.LOG_TAG,
            "Module v${BuildConfig.VERSION_NAME} on $frameworkName $frameworkVersion",
        )
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        val prefs =
            runCatching { getRemotePreferences(Prefs.GROUP) }
                .onFailure { log("Remote prefs unavailable, using defaults", it) }
                .getOrNull()

        prefs?.let { p ->
            Logger.debugEnabled = Prefs.DEBUG_LOGS.read(p)
            p.registerOnSharedPreferenceChangeListener { _, _ ->
                Logger.debugEnabled = Prefs.DEBUG_LOGS.read(p)
            }
        }

        runCatching {
            SystemServerHooks.hook(this, param, prefs)
        }.onSuccess { log("system_server hooks applied") }
            .onFailure { log("system_server hook registration failed", it) }
    }
}
