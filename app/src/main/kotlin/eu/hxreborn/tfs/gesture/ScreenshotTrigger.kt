package eu.hxreborn.tfs.gesture

import android.os.Handler
import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import eu.hxreborn.tfs.util.log
import java.lang.reflect.Method

private const val TAKE_SCREENSHOT_FULLSCREEN = 1
private const val SCREENSHOT_VENDOR_GESTURE = 6
private const val INJECT_INPUT_EVENT_MODE_ASYNC = 0
private const val SCREENSHOT_KEYCODE = KeyEvent.KEYCODE_SYSRQ

data class ScreenshotDispatch(
    val target: Any,
    val method: Method,
    val mode: ScreenshotMode,
    val handler: Handler,
)

enum class ScreenshotMode {
    DirectTakeScreenshot,
    InjectSysrqKeyEvent,
}

object ScreenshotTrigger {
    fun takeScreenshot(dispatch: ScreenshotDispatch?) {
        if (dispatch == null) {
            log("No screenshot dispatch path is available")
            return
        }
        log(
            "Queueing screenshot [mode=${dispatch.mode} method=${dispatch.methodSummary()} " +
                "handlerThread=${dispatch.handler.looper.thread.name}]",
        )

        val posted =
            dispatch.handler.post {
                dispatch.runDispatch("handler")
            }

        log(
            "Screenshot post result [posted=$posted mode=${dispatch.mode} " +
                "method=${dispatch.methodSummary()}]",
        )

        if (!posted) {
            log(
                "Screenshot post failed, falling back to direct dispatch " +
                    "[mode=${dispatch.mode} method=${dispatch.methodSummary()}]",
            )
            dispatch.runDispatch("fallback")
        }
    }

    private fun ScreenshotDispatch.runDispatch(origin: String) {
        runCatching {
            log(
                "Taking screenshot [origin=$origin mode=$mode method=${methodSummary()}]",
            )
            when (mode) {
                ScreenshotMode.DirectTakeScreenshot -> invokeDirect()
                ScreenshotMode.InjectSysrqKeyEvent -> injectSysrq()
            }
        }.onFailure { log("takeScreenshot failed [origin=$origin method=${methodSummary()}]", it) }
    }

    private fun ScreenshotDispatch.invokeDirect() =
        when (method.parameterCount) {
            2 -> method.invoke(target, TAKE_SCREENSHOT_FULLSCREEN, SCREENSHOT_VENDOR_GESTURE)
            1 -> method.invoke(target, TAKE_SCREENSHOT_FULLSCREEN)
            else -> log("Unsupported takeScreenshot signature: ${method.parameterCount} params")
        }

    private fun ScreenshotDispatch.injectSysrq() {
        val downTime = SystemClock.uptimeMillis()

        fun keyEvent(
            action: Int,
            eventTime: Long,
        ) = KeyEvent(
            downTime,
            eventTime,
            action,
            SCREENSHOT_KEYCODE,
            0,
            0,
            KeyCharacterMap.VIRTUAL_KEYBOARD,
            0,
            KeyEvent.FLAG_FROM_SYSTEM,
            InputDevice.SOURCE_KEYBOARD,
        )

        method.invoke(
            target,
            keyEvent(KeyEvent.ACTION_DOWN, downTime),
            INJECT_INPUT_EVENT_MODE_ASYNC,
        )
        method.invoke(target, keyEvent(KeyEvent.ACTION_UP, downTime), INJECT_INPUT_EVENT_MODE_ASYNC)
    }

    private fun ScreenshotDispatch.methodSummary(): String =
        buildString {
            append(method.declaringClass.name)
            append('#')
            append(method.name)
            append('(')
            append(method.parameterTypes.joinToString(",") { it.simpleName ?: it.name })
            append(')')
        }
}
