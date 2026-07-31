package eu.hxreborn.tfs.action

import android.content.Context
import eu.hxreborn.tfs.action.flashlight.ToggleFlashlightAction
import eu.hxreborn.tfs.action.ringer.RingerModeAction
import eu.hxreborn.tfs.action.screenoff.ScreenOffAction
import eu.hxreborn.tfs.action.screenshot.ScreenshotAction
import eu.hxreborn.tfs.action.screenshot.ScreenshotDispatch
import eu.hxreborn.tfs.action.splitscreen.ToggleSplitScreenAction
import eu.hxreborn.tfs.prefs.SplitMethod

object ActionRegistry {
    val all: List<ActionId> = ActionId.entries

    fun build(
        id: ActionId,
        context: Context,
        dispatch: ScreenshotDispatch?,
        serverClassLoader: ClassLoader?,
        splitMethod: () -> SplitMethod,
    ): Action =
        when (id) {
            ActionId.NO_ACTION -> {
                Action {}
            }

            ActionId.SCREENSHOT -> {
                ScreenshotAction(dispatch)
            }

            ActionId.SCREEN_OFF -> {
                ScreenOffAction(context)
            }

            ActionId.TOGGLE_FLASHLIGHT -> {
                ToggleFlashlightAction(context)
            }

            ActionId.RINGER_MODE -> {
                RingerModeAction(context)
            }

            ActionId.TOGGLE_SPLIT_SCREEN -> {
                ToggleSplitScreenAction(context, serverClassLoader, splitMethod)
            }
        }
}
