package eu.hxreborn.tfs.action.screenshot

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyCharacterMap
import android.view.KeyEvent
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.util.findAllMethodsUpward
import eu.hxreborn.tfs.util.findMethodUpward
import eu.hxreborn.tfs.util.log
import eu.hxreborn.tfs.util.readField
import eu.hxreborn.tfs.util.signature
import java.lang.reflect.Method
import java.util.function.Consumer

private const val TAKE_SCREENSHOT_FULLSCREEN = 1
private const val SCREENSHOT_VENDOR_GESTURE = 6
private const val INJECT_INPUT_EVENT_MODE_ASYNC = 0
private const val MAX_DISPLAY_POLICY_ARGS = 4

class ScreenshotDispatch(
    val handler: Handler,
    internal val invocation: () -> Unit,
    val description: String,
)

internal object ScreenshotActionResolver {
    fun resolve(
        phoneWindowManager: Any,
        handler: Handler,
        captureMode: CaptureMode = CaptureMode.REFLECTION,
    ): ScreenshotDispatch? {
        val dispatch =
            when (captureMode) {
                CaptureMode.SYSRQ -> {
                    resolveSysrq(phoneWindowManager, handler)
                        ?: resolveDisplayPolicy(phoneWindowManager, handler)
                        ?: resolveScreenshotHelper(
                            phoneWindowManager,
                            handler,
                        )
                }

                CaptureMode.REFLECTION -> {
                    resolveDisplayPolicy(phoneWindowManager, handler) ?: resolveScreenshotHelper(
                        phoneWindowManager,
                        handler,
                    )
                }
            }
        if (dispatch == null) {
            log(
                "ScreenshotActionResolver: no screenshot method found — screenshot unavailable",
            )
        }
        return dispatch
    }

    // ── SYSRQ path ──────────────────────────────────────────────────────
    // Fake a screenshot by injecting KEYCODE_SYSRQ
    // Apps can eat this key first, so this stays a last resort

    private fun resolveSysrq(
        phoneWindowManager: Any,
        handler: Handler,
    ): ScreenshotDispatch? {
        val inputManager = phoneWindowManager.readField("mInputManager") ?: return null
        val method =
            inputManager.javaClass.findMethodUpward(
                "injectInputEvent",
                InputEvent::class.java,
                Int::class.javaPrimitiveType!!,
            ) ?: run {
                log("ScreenshotActionResolver: injectInputEvent not found for SYSRQ path")
                return null
            }

        val summary = "SYSRQ ${method.signature()} mode=$INJECT_INPUT_EVENT_MODE_ASYNC"
        log("ScreenshotActionResolver: resolved $summary")

        return ScreenshotDispatch(
            handler = handler,
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

    // ── DisplayPolicy path ──────────────────────────────────────────────
    // Try the direct framework screenshot path first
    // This skips app key handling
    //
    // AOSP reference:
    // https://cs.android.com/android/platform/superproject/main/+/main:services/core/java/com/android/server/wm/DisplayPolicy.java

    private fun resolveDisplayPolicy(
        phoneWindowManager: Any,
        handler: Handler,
    ): ScreenshotDispatch? {
        val target = phoneWindowManager.readField("mDefaultDisplayPolicy") ?: phoneWindowManager

        // Exact: takeScreenshot(int, int)
        target.javaClass
            .findMethodUpward(
                "takeScreenshot",
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
            )?.let { m ->
                return dispatchDisplayPolicy(target, m, handler, "exact(int,int)")
            }

        // Exact: takeScreenshot(int)
        target.javaClass
            .findMethodUpward(
                "takeScreenshot",
                Int::class.javaPrimitiveType!!,
            )?.let { m ->
                return dispatchDisplayPolicy(target, m, handler, "exact(int)")
            }

        // Runtime scan: any takeScreenshot with int-only params (up to 4)
        target.javaClass
            .findAllMethodsUpward("takeScreenshot")
            .filter { m ->
                m.parameterTypes.all { it == Int::class.javaPrimitiveType } &&
                    m.parameterCount in 1..MAX_DISPLAY_POLICY_ARGS
            }.maxByOrNull { it.parameterCount }
            ?.let { m ->
                return dispatchDisplayPolicy(target, m, handler, "scan")
            }

        log("ScreenshotActionResolver: takeScreenshot not found on DisplayPolicy")
        return null
    }

    private fun dispatchDisplayPolicy(
        target: Any,
        method: Method,
        handler: Handler,
        origin: String,
    ): ScreenshotDispatch {
        val defaults = intArrayOf(TAKE_SCREENSHOT_FULLSCREEN, SCREENSHOT_VENDOR_GESTURE, 0, 0)
        val args = defaults.take(method.parameterCount).map { it as Any }.toTypedArray()
        val summary = "DisplayPolicy[$origin] ${method.signature()}"
        log("ScreenshotActionResolver: resolved $summary")
        return ScreenshotDispatch(
            handler = handler,
            invocation = { method.invoke(target, *args) },
            description = summary,
        )
    }

    // ── ScreenshotHelper path ───────────────────────────────────────────
    // Some Pixel Android 16 builds do not expose DisplayPolicy.takeScreenshot()
    // ScreenshotHelper still hits the real system screenshot path
    //
    // Do not use SYSRQ here
    // PhoneWindowManager handles SYSRQ in interceptUnhandledKey(), so the app
    // can eat the key before screenshot handling runs
    //
    // AOSP reference:
    // https://cs.android.com/android/platform/superproject/main/+/main:services/core/java/com/android/server/policy/PhoneWindowManager.java

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

            // Prefer the field already on DisplayPolicy/PhoneWindowManager
            val displayPolicy = phoneWindowManager.readField("mDefaultDisplayPolicy")
            val displayPolicyHelper = displayPolicy?.readField("mScreenshotHelper")
            val helper =
                phoneWindowManager.readField("mScreenshotHelper") ?: displayPolicyHelper
                    ?: helperClass.getConstructor(Context::class.java).newInstance(context)

            // takeScreenshot(int source, Handler handler, Consumer<Uri> completion)
            val method =
                helperClass.findMethodUpward(
                    "takeScreenshot",
                    Int::class.javaPrimitiveType!!,
                    Handler::class.java,
                    Consumer::class.java,
                ) ?: return null

            val summary = "ScreenshotHelper ${method.signature()}"
            log("ScreenshotActionResolver: resolved $summary")

            ScreenshotDispatch(
                handler = handler,
                invocation = { method.invoke(helper, SCREENSHOT_VENDOR_GESTURE, handler, null) },
                description = summary,
            )
        }.onFailure {
            log("ScreenshotActionResolver: ScreenshotHelper resolution failed", it)
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
