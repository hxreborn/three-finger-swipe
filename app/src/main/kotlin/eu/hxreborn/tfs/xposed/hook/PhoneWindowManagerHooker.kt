package eu.hxreborn.tfs.xposed.hook

import android.content.SharedPreferences
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.action.ActionRegistry
import eu.hxreborn.tfs.gesture.GestureConfig
import eu.hxreborn.tfs.gesture.GestureInputMonitor
import eu.hxreborn.tfs.gesture.GestureHandler
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.readOrDefault
import eu.hxreborn.tfs.util.log
import io.github.libxposed.api.XposedInterface.AfterHookCallback
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.XposedHooker
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean

@XposedHooker
class PhoneWindowManagerHooker : Hooker {
    companion object {
        private val registered = AtomicBoolean(false)
        private var prefs: SharedPreferences? = null

        fun init(prefs: SharedPreferences?) {
            this.prefs = prefs
        }

        @JvmStatic
        @AfterInvocation
        fun after(callback: AfterHookCallback) {
            // SystemReady can hit again
            // Register once or duplicate listeners start stacking up
            if (!registered.compareAndSet(false, true)) return

            runCatching {
                val phoneWindowManager = callback.thisObject ?: error("PhoneWindowManager missing")
                registerGestureListener(phoneWindowManager)
            }.onSuccess { log("Three-finger gesture listener registered") }.onFailure {
                registered.set(false)
                log("Gesture listener registration failed", it)
            }
        }

        private fun registerGestureListener(phoneWindowManager: Any) {
            val p = prefs
            val captureMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.readOrDefault(p))
            val config =
                GestureConfig(
                    swipeThresholdFraction = Prefs.SWIPE_THRESHOLD_PCT.readOrDefault(p) / 100f,
                    edgeExclusionDp = Prefs.EDGE_EXCLUSION_DP.readOrDefault(p).toFloat(),
                    fingerLandingWindowMs = Prefs.FINGER_LANDING_MS.readOrDefault(p).toLong(),
                    cooldownMs = Prefs.COOLDOWN_MS.readOrDefault(p).toLong(),
                )
            val bindings = PhoneWindowManagerBindings.resolve(phoneWindowManager, captureMode)
            val actionId = ActionId.fromKey(Prefs.SELECTED_ACTION.readOrDefault(p))
            val action = ActionRegistry.build(actionId, bindings.screenshotDispatch)
            // Build the monitor before wiring the listener
            // Block the app from handling this gesture
            GestureInputMonitor.create()
            val gestureHandler =
                GestureHandler(
                    context = bindings.systemContext,
                    prefs = p,
                    config = config,
                    onTrigger = action::execute,
                    onPilfer = { GestureInputMonitor.pilferPointers() },
                )
            val proxy =
                Proxy.newProxyInstance(
                    // PointerEventListener is hidden and comes from system_server's classloader
                    // Proxy avoids shipping a stub that can drift across releases
                    // https://cs.android.com/android/platform/superproject/main/+/main:core/java/android/view/WindowManagerPolicyConstants.java
                    phoneWindowManager.javaClass.classLoader,
                    arrayOf(bindings.pointerListenerClass),
                    PointerEventListenerProxy(gestureHandler),
                )
            bindings.pointerRegistration.invoke(proxy)
        }
    }
}
