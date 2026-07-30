package eu.hxreborn.tfs.action.ringer

import android.content.Context
import android.media.AudioManager
import eu.hxreborn.tfs.action.Action
import eu.hxreborn.tfs.util.Logger

class RingerModeAction(
    context: Context,
) : Action {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun execute() {
        runCatching {
            val next =
                when (audioManager.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                    AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                    else -> AudioManager.RINGER_MODE_NORMAL
                }
            audioManager.ringerMode = next
            next
        }.onSuccess {
            Logger.info("ringer changed mode=$it")
        }.onFailure {
            Logger.error("ringer change failed", it)
        }
    }
}
