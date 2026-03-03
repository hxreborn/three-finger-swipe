package eu.hxreborn.tfs.action

import eu.hxreborn.tfs.action.screenshot.ScreenshotAction
import eu.hxreborn.tfs.action.screenshot.ScreenshotDispatch

object ActionRegistry {
    val all: List<ActionId> = ActionId.entries

    fun build(
        id: ActionId,
        dispatch: ScreenshotDispatch?,
    ): Action =
        when (id) {
            ActionId.NO_ACTION -> Action {}
            ActionId.SCREENSHOT -> ScreenshotAction(dispatch)
            else -> StubAction(id)
        }
}
