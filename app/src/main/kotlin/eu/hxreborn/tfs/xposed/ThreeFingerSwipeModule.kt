package eu.hxreborn.tfs.xposed

import android.util.Log
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.ModuleConstants
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.log
import eu.hxreborn.tfs.xposed.hook.debugLogs
import eu.hxreborn.tfs.xposed.hook.loadHookPrefs
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

// Lowercase top-level handle assigned in onModuleLoaded so hook helpers can call module.log
// without reaching through a companion singleton. Matches the canon used by other hxreborn modules.
@PublishedApi
internal lateinit var module: ThreeFingerSwipeModule

class ThreeFingerSwipeModule : XposedModule() {
    override fun onModuleLoaded(param: ModuleLoadedParam) {
        module = this
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
            loadHookPrefs(p)
            Logger.debugEnabled = debugLogs
            // Strong ref kept on the prefs handle (cached above) so the listener survives GC.
            // Fires cross-process when the companion app commits a change.
            p.registerOnSharedPreferenceChangeListener { _, _ ->
                loadHookPrefs(p)
                Logger.debugEnabled = debugLogs
            }
        }

        runCatching {
            SystemServerHooks.hook(this, param)
        }.onSuccess { log("system_server hooks applied") }
            .onFailure { log("system_server hook registration failed", it) }
    }
}
