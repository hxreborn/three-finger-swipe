package eu.hxreborn.tfs.action.screenshot

import eu.hxreborn.tfs.action.Action
import eu.hxreborn.tfs.util.Logger

class ScreenshotAction(
    private val dispatch: ScreenshotDispatch?,
) : Action {
    override fun execute() {
        val dispatch = dispatch
        if (dispatch == null) {
            Logger.warn("screenshot skipped reason=no-dispatch")
            return
        }
        runCatching {
            dispatch.invocation()
        }.onSuccess {
            Logger.info("screenshot requested path=${dispatch.description}")
        }.onFailure {
            Logger.error("screenshot request failed path=${dispatch.description}", it)
        }
    }
}
