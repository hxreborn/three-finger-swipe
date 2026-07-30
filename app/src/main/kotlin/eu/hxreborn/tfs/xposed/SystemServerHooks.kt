package eu.hxreborn.tfs.xposed

import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.methodAccessible
import eu.hxreborn.tfs.xposed.hook.PhoneWindowManagerHook
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

object SystemServerHooks {
    fun install(
        module: XposedModule,
        param: SystemServerStartingParam,
    ) {
        hookSystemReady(module, param.classLoader)
    }

    // the live PhoneWindowManager is only reachable after systemReady
    private fun hookSystemReady(
        module: XposedModule,
        classLoader: ClassLoader,
    ) {
        val phoneWindowManager =
            classLoader.loadClass("com.android.server.policy.PhoneWindowManager")
        module
            .hook(phoneWindowManager.methodAccessible("systemReady"))
            .intercept(PhoneWindowManagerHook.createInterceptor())
        Logger.info("hooked PhoneWindowManager.systemReady")
    }
}
