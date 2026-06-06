package eu.hxreborn.tfs.xposed.hook

import android.content.SharedPreferences
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.action.ActionRegistry
import eu.hxreborn.tfs.gesture.GestureConfig
import eu.hxreborn.tfs.gesture.GestureHandler
import eu.hxreborn.tfs.gesture.GestureInputMonitor
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.readOrDefault
import eu.hxreborn.tfs.util.log
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean

object PhoneWindowManagerHook {
    private val registered = AtomicBoolean(false)
    private var prefs: SharedPreferences? = null

    fun init(prefs: SharedPreferences?) {
        this.prefs = prefs
    }

    fun createInterceptor(): XposedInterface.Hooker =
        XposedInterface.Hooker { chain ->
            val result = chain.proceed()
            if (!registered.compareAndSet(false, true)) return@Hooker result
            runCatching {
                val pwm = chain.thisObject ?: error("PhoneWindowManager missing")
                registerGestureListener(pwm)
            }.onSuccess {
                log("Three-finger gesture listener registered")
            }.onFailure {
                registered.set(false)
                log("Gesture listener registration failed", it)
            }
            result
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
        val actions =
            ActionId.entries.associateWith { id ->
                ActionRegistry.build(id, bindings.systemContext, bindings.screenshotDispatch)
            }

        // Pilfering blocks the app from receiving touch events during the gesture
        GestureInputMonitor.create()
        val gestureHandler =
            GestureHandler(
                context = bindings.systemContext,
                prefs = p,
                config = config,
                onTrigger = {
                    val id = ActionId.fromKey(Prefs.SELECTED_ACTION.readOrDefault(p))
                    actions[id]?.execute()
                },
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
