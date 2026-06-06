package eu.hxreborn.tfs.xposed

import android.content.SharedPreferences
import eu.hxreborn.tfs.util.log
import eu.hxreborn.tfs.util.methodAccessible
import eu.hxreborn.tfs.xposed.hook.PhoneWindowManagerHook
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

object SystemServerHooks {
    fun hook(
        module: XposedModule,
        param: SystemServerStartingParam,
        prefs: SharedPreferences?,
    ) {
        PhoneWindowManagerHook.init(prefs)
        val phoneWindowManager =
            param.classLoader.loadClass(
                "com.android.server.policy.PhoneWindowManager",
            )
        module
            .hook(phoneWindowManager.methodAccessible("systemReady"))
            .intercept(PhoneWindowManagerHook.createInterceptor())
        log("Registered PhoneWindowManager.systemReady hook")
    }
}
