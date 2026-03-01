package eu.hxreborn.tfs.xposed.hook

import android.content.SharedPreferences
import eu.hxreborn.tfs.gesture.GestureInputMonitor
import eu.hxreborn.tfs.gesture.ScreenshotTrigger
import eu.hxreborn.tfs.gesture.ThreeFingerGestureHandler
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
            val bindings = PhoneWindowManagerBindings.resolve(phoneWindowManager)
            GestureInputMonitor.create()
            val gestureHandler =
                ThreeFingerGestureHandler(
                    context = bindings.systemContext,
                    prefs = prefs,
                    onSwipeDown = { ScreenshotTrigger.takeScreenshot(bindings.screenshotDispatch) },
                    onPilfer = { GestureInputMonitor.pilferPointers() },
                )
            val proxy =
                Proxy.newProxyInstance(
                    phoneWindowManager.javaClass.classLoader,
                    arrayOf(bindings.pointerListenerClass),
                    PointerEventListenerProxy(gestureHandler),
                )
            bindings.pointerRegistration.invoke(proxy)
        }
    }
}
