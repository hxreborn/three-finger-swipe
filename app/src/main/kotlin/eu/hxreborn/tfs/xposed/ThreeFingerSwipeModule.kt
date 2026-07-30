package eu.hxreborn.tfs.xposed

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.xposed.hook.PhoneWindowManagerHook
import eu.hxreborn.tfs.xposed.hook.loadHookPrefs
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.HotReloadedParam
import io.github.libxposed.api.XposedModuleInterface.HotReloadingParam
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

@PublishedApi
internal lateinit var module: ThreeFingerSwipeModule
    private set

class ThreeFingerSwipeModule : XposedModule() {
    private val prefsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sp, _ ->
            runCatching { loadHookPrefs(sp) }.onFailure { Logger.error("prefs refresh failed", it) }
        }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        module = this
        Logger.info(
            "module loaded version=${BuildConfig.VERSION_NAME} " +
                "framework=$frameworkName frameworkVersion=$frameworkVersion",
        )
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        registerPrefsListener()

        try {
            SystemServerHooks.install(this, param)
        } catch (e: Exception) {
            Logger.error("system_server hook registration failed", e)
        }
    }

    override fun onHotReloading(param: HotReloadingParam): Boolean {
        val phoneWindowManager =
            try {
                PhoneWindowManagerHook.teardown()
            } catch (e: Exception) {
                Logger.warn("hot reload rejected reason=teardown-failed", e)
                return false
            }
        if (phoneWindowManager == null) {
            Logger.warn("hot reload rejected reason=phone-window-manager-unavailable")
            return false
        }

        param.setSavedInstanceState(phoneWindowManager)
        Logger.info("hot reload old generation retired")
        return true
    }

    override fun onHotReloaded(param: HotReloadedParam) {
        module = this
        registerPrefsListener()
        param.oldHookHandles.forEach { handle ->
            try {
                handle.unhook()
            } catch (e: Exception) {
                Logger.warn("hot reload old hook removal failed", e)
            }
        }

        val phoneWindowManager = param.savedInstanceState
        if (phoneWindowManager == null) {
            Logger.warn("hot reload restore skipped reason=phone-window-manager-unavailable")
            return
        }

        // input monitor setup needs the main thread's Choreographer
        val posted =
            Handler(Looper.getMainLooper()).post {
                try {
                    PhoneWindowManagerHook.install(phoneWindowManager, reloaded = true)
                } catch (e: Exception) {
                    Logger.error("hot reload gesture registration failed", e)
                }
            }
        if (!posted) {
            Logger.warn("hot reload restore skipped reason=main-handler-rejected")
        }
    }

    private fun registerPrefsListener() {
        val prefs =
            try {
                getRemotePreferences(Prefs.GROUP)
            } catch (e: Exception) {
                Logger.warn("remote prefs unavailable defaults=true", e)
                return
            }
        loadHookPrefs(prefs)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }
}
