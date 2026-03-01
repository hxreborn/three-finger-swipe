package eu.hxreborn.tfs.gesture

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import eu.hxreborn.tfs.util.log
import java.lang.reflect.Method

private const val TAKE_SCREENSHOT_FULLSCREEN = 1
private const val TAKE_SCREENSHOT_SELECTED_REGION = 2
private const val SCREENSHOT_VENDOR_GESTURE = 6
private const val INJECT_INPUT_EVENT_MODE_ASYNC = 0
private const val SCREENSHOT_KEYCODE = KeyEvent.KEYCODE_SYSRQ

data class ScreenshotDispatch(
    val target: Any,
    val method: Method,
    val mode: ScreenshotMode,
    val displayPolicy: Any,
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
        dispatch.handler.post {
            runCatching {
                when (dispatch.mode) {
                    ScreenshotMode.DirectTakeScreenshot -> dispatch.invokeDirect()
                    ScreenshotMode.InjectSysrqKeyEvent -> dispatch.injectSysrq()
                }
            }.onFailure { log("takeScreenshot failed", it) }
        }
    }

    fun takeCropScreenshot(
        dispatch: ScreenshotDispatch?,
        context: Context,
    ) {
        if (dispatch == null) {
            log("No screenshot dispatch path is available")
            return
        }
        dispatch.handler.post {
            runCatching { requestSystemUICrop(dispatch, context) }.onFailure {
                log("SystemUI crop request failed, falling back to custom capture", it)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    runCatching {
                        CropCapture.show(context, dispatch.displayPolicy)
                    }.onFailure { e -> log("CropCapture.show failed", e) }
                } else {
                    log("CropCapture requires API 31+, skipping")
                }
            }
        }
    }

    private fun requestSystemUICrop(
        dispatch: ScreenshotDispatch,
        context: Context,
    ) {
        val cl = dispatch.displayPolicy.javaClass.classLoader
        val helperClass =
            Class.forName(
                "com.android.internal.util.ScreenshotHelper",
                false,
                cl,
            )
        val screenshotHelper =
            helperClass.getDeclaredConstructor(Context::class.java).newInstance(context)
        val builderClass =
            Class.forName(
                "com.android.internal.util.ScreenshotRequest\$Builder",
                false,
                cl,
            )
        val builder =
            builderClass
                .getDeclaredConstructor(
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                ).newInstance(TAKE_SCREENSHOT_SELECTED_REGION, SCREENSHOT_VENDOR_GESTURE)
        val request = builderClass.getMethod("build").invoke(builder)

        val takeScreenshot =
            screenshotHelper.javaClass.methods.firstOrNull {
                it.name == "takeScreenshot" &&
                    it.parameterCount == 3
            }
                ?: error("takeScreenshot not found on ScreenshotHelper")
        takeScreenshot.isAccessible = true
        takeScreenshot.invoke(screenshotHelper, request, dispatch.handler, null)
        log("Crop screenshot requested via SystemUI (type=$TAKE_SCREENSHOT_SELECTED_REGION)")
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
}
