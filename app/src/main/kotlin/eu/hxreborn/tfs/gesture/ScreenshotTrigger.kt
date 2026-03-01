package eu.hxreborn.tfs.gesture

import android.os.Handler
import eu.hxreborn.tfs.util.log

class ScreenshotDispatch(
    val handler: Handler,
    private val invocation: () -> Unit,
    val description: String,
) {
    fun invoke() = invocation()
}

object ScreenshotTrigger {
    fun takeScreenshot(dispatch: ScreenshotDispatch?) {
        dispatch?.dispatchOrFallback() ?: log("No screenshot dispatch path is available")
    }

    private fun ScreenshotDispatch.dispatchOrFallback() {
        if (!handler.post { runDispatch(this, "handler") }) {
            runDispatch(this, "fallback")
        }
    }

    private fun runDispatch(
        dispatch: ScreenshotDispatch,
        origin: String,
    ) {
        runCatching {
            dispatch.invoke()
        }.onSuccess {
            log("Screenshot request sent [origin=$origin ${dispatch.description}]")
        }.onFailure {
            log("takeScreenshot failed [origin=$origin ${dispatch.description}]", it)
        }
    }
}
