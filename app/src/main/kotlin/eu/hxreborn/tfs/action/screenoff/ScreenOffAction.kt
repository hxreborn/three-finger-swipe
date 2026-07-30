package eu.hxreborn.tfs.action.screenoff

import android.content.Context
import android.os.PowerManager
import android.os.SystemClock
import eu.hxreborn.tfs.action.Action
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.findMethodUpward

class ScreenOffAction(
    context: Context,
) : Action {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val goToSleep =
        powerManager.javaClass.findMethodUpward(
            "goToSleep",
            Long::class.javaPrimitiveType!!,
        )

    override fun execute() {
        runCatching {
            val method = goToSleep ?: error("PowerManager.goToSleep(long) unavailable")
            method.invoke(powerManager, SystemClock.uptimeMillis())
        }.onSuccess {
            Logger.info("screen off")
        }.onFailure {
            Logger.error("screen off failed", it)
        }
    }
}
