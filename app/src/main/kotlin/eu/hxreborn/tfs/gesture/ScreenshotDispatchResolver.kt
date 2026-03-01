package eu.hxreborn.tfs.gesture

import android.os.Handler
import android.view.InputEvent
import eu.hxreborn.tfs.util.findMethodUpward
import eu.hxreborn.tfs.util.readField

internal object ScreenshotDispatchResolver {
    fun resolve(
        phoneWindowManager: Any,
        handler: Handler,
    ): ScreenshotDispatch? {
        val screenshotTarget =
            phoneWindowManager.readField("mDefaultDisplayPolicy") ?: phoneWindowManager

        screenshotTarget.javaClass
            .findMethodUpward(
                "takeScreenshot",
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
            )?.let {
                return ScreenshotDispatch(
                    target = screenshotTarget,
                    method = it,
                    mode = ScreenshotMode.DirectTakeScreenshot,
                    displayPolicy = screenshotTarget,
                    handler = handler,
                )
            }

        screenshotTarget.javaClass
            .findMethodUpward(
                "takeScreenshot",
                Int::class.javaPrimitiveType!!,
            )?.let {
                return ScreenshotDispatch(
                    target = screenshotTarget,
                    method = it,
                    mode = ScreenshotMode.DirectTakeScreenshot,
                    displayPolicy = screenshotTarget,
                    handler = handler,
                )
            }

        val inputManager = phoneWindowManager.readField("mInputManager") ?: return null
        val injectInputEvent =
            inputManager.javaClass.findMethodUpward(
                "injectInputEvent",
                InputEvent::class.java,
                Int::class.javaPrimitiveType!!,
            ) ?: return null

        return ScreenshotDispatch(
            target = inputManager,
            method = injectInputEvent,
            mode = ScreenshotMode.InjectSysrqKeyEvent,
            displayPolicy = screenshotTarget,
            handler = handler,
        )
    }
}
