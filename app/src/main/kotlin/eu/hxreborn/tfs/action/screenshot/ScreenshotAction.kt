package eu.hxreborn.tfs.action.screenshot

import eu.hxreborn.tfs.action.Action
import eu.hxreborn.tfs.util.log

class ScreenshotAction(
    private val dispatch: ScreenshotDispatch?,
) : Action {
    override fun execute() {
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
            dispatch.invocation()
        }.onSuccess {
            log("Screenshot request sent [origin=$origin ${dispatch.description}]")
        }.onFailure {
            log("takeScreenshot failed [origin=$origin ${dispatch.description}]", it)
        }
    }
}
