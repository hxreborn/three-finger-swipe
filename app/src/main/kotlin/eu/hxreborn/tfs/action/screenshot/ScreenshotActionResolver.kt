package eu.hxreborn.tfs.action.screenshot

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyCharacterMap
import android.view.KeyEvent
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.findAllMethodsUpward
import eu.hxreborn.tfs.util.findMethodUpward
import eu.hxreborn.tfs.util.readField
import eu.hxreborn.tfs.util.signature
import java.lang.reflect.Method
import java.util.function.Consumer

private const val TAKE_SCREENSHOT_FULLSCREEN = 1
private const val SCREENSHOT_VENDOR_GESTURE = 6
private const val INJECT_INPUT_EVENT_MODE_ASYNC = 0
private const val MAX_DISPLAY_POLICY_ARGS = 4

class ScreenshotDispatch(
    internal val invocation: () -> Unit,
    val description: String,
)

internal object ScreenshotActionResolver {
    fun resolve(
        phoneWindowManager: Any,
        handler: Handler,
        captureMode: CaptureMode = CaptureMode.SYSTEM_API,
    ): ScreenshotDispatch? =
        when (captureMode) {
            CaptureMode.SYSRQ -> {
                resolveSysrq(phoneWindowManager) ?: resolveDisplayPolicy(phoneWindowManager)
                    ?: resolveScreenshotHelper(phoneWindowManager, handler)
            }

            CaptureMode.SYSTEM_API -> {
                resolveDisplayPolicy(phoneWindowManager)
                    ?: resolveScreenshotHelper(phoneWindowManager, handler)
            }
        }.also {
            if (it == null) {
                Logger.warn("screenshot unavailable reason=no-method")
            }
        }

    // SYSRQ injection is last resort only because apps can eat it before PWM handles it
    private fun resolveSysrq(phoneWindowManager: Any): ScreenshotDispatch? {
        val inputManager = phoneWindowManager.readField("mInputManager") ?: return null
        val method =
            inputManager.javaClass.findMethodUpward(
                "injectInputEvent",
                InputEvent::class.java,
                Int::class.javaPrimitiveType!!,
            ) ?: run {
                Logger.warn("screenshot path unavailable reason=sysrq")
                return null
            }

        val summary = "SYSRQ ${method.signature()} mode=$INJECT_INPUT_EVENT_MODE_ASYNC"
        Logger.info("screenshot path resolved mode=sysrq method=${method.signature()}")

        return ScreenshotDispatch(
            invocation = {
                val now = SystemClock.uptimeMillis()
                method.invoke(
                    inputManager,
                    sysrqEvent(KeyEvent.ACTION_DOWN, now),
                    INJECT_INPUT_EVENT_MODE_ASYNC,
                )
                method.invoke(
                    inputManager,
                    sysrqEvent(KeyEvent.ACTION_UP, now),
                    INJECT_INPUT_EVENT_MODE_ASYNC,
                )
            },
            description = summary,
        )
    }

    // same path the system uses for hardware button screenshots and skips app key handling
    // DisplayPolicy.takeScreenshot() exists from A9 to A14 and is gone in A15
    // https://cs.android.com/android/platform/superproject/main/+/main:services/core/java/com/android/server/wm/DisplayPolicy.java
    private fun resolveDisplayPolicy(phoneWindowManager: Any): ScreenshotDispatch? {
        val target = phoneWindowManager.readField("mDefaultDisplayPolicy") ?: phoneWindowManager

        target.javaClass
            .findMethodUpward(
                "takeScreenshot",
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
            )?.let { m ->
                return dispatchDisplayPolicy(target, m, "exact(int,int)")
            }

        target.javaClass
            .findMethodUpward(
                "takeScreenshot",
                Int::class.javaPrimitiveType!!,
            )?.let { m ->
                return dispatchDisplayPolicy(target, m, "exact(int)")
            }

        target.javaClass
            .findAllMethodsUpward("takeScreenshot")
            .filter { m ->
                m.parameterTypes.all { it == Int::class.javaPrimitiveType } &&
                    m.parameterCount in 1..MAX_DISPLAY_POLICY_ARGS
            }.maxByOrNull { it.parameterCount }
            ?.let { m ->
                return dispatchDisplayPolicy(target, m, "scan")
            }

        Logger.warn("screenshot path unavailable reason=display-policy")
        return null
    }

    private fun dispatchDisplayPolicy(
        target: Any,
        method: Method,
        origin: String,
    ): ScreenshotDispatch {
        val defaults = intArrayOf(TAKE_SCREENSHOT_FULLSCREEN, SCREENSHOT_VENDOR_GESTURE, 0, 0)
        val args = defaults.take(method.parameterCount).map { it as Any }.toTypedArray()
        val summary = "DisplayPolicy[$origin] ${method.signature()}"
        Logger.info("screenshot path resolved mode=display-policy method=${method.signature()}")
        return ScreenshotDispatch(
            invocation = { method.invoke(target, *args) },
            description = summary,
        )
    }

    // fallback for A15+ where DisplayPolicy.takeScreenshot() is gone
    // https://cs.android.com/android/platform/superproject/main/+/main:core/java/com/android/internal/util/ScreenshotHelper.java
    private fun resolveScreenshotHelper(
        phoneWindowManager: Any,
        handler: Handler,
    ): ScreenshotDispatch? =
        runCatching {
            val classLoader = phoneWindowManager.javaClass.classLoader
            val context = phoneWindowManager.readField("mContext") as? Context ?: return null

            val helperClass =
                Class.forName(
                    "com.android.internal.util.ScreenshotHelper",
                    false,
                    classLoader,
                )

            // prefers the field already on DisplayPolicy or PhoneWindowManager
            val displayPolicy = phoneWindowManager.readField("mDefaultDisplayPolicy")
            val displayPolicyHelper = displayPolicy?.readField("mScreenshotHelper")
            val helper =
                phoneWindowManager.readField("mScreenshotHelper") ?: displayPolicyHelper
                    ?: helperClass.getConstructor(Context::class.java).newInstance(context)

            val method =
                helperClass.findMethodUpward(
                    "takeScreenshot",
                    Int::class.javaPrimitiveType!!,
                    Handler::class.java,
                    Consumer::class.java,
                ) ?: return null

            val summary = "ScreenshotHelper ${method.signature()}"
            Logger.info("screenshot path resolved mode=helper method=${method.signature()}")

            ScreenshotDispatch(
                invocation = { method.invoke(helper, SCREENSHOT_VENDOR_GESTURE, handler, null) },
                description = summary,
            )
        }.onFailure {
            Logger.warn("screenshot path unavailable reason=helper", it)
        }.getOrNull()
}

private fun sysrqEvent(
    action: Int,
    time: Long,
) = KeyEvent(
    time,
    time,
    action,
    KeyEvent.KEYCODE_SYSRQ,
    0,
    0,
    KeyCharacterMap.VIRTUAL_KEYBOARD,
    0,
    0,
    InputDevice.SOURCE_KEYBOARD,
)
