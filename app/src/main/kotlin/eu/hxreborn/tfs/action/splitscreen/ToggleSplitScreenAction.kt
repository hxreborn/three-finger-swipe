package eu.hxreborn.tfs.action.splitscreen

import android.app.ActivityManager
import android.content.Context
import android.view.Display
import eu.hxreborn.tfs.action.Action
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.findMethodUpward
import eu.hxreborn.tfs.util.signature
import java.lang.reflect.Method

private const val WINDOWING_MODE_MULTI_WINDOW = 6

class ToggleSplitScreenAction(
    private val context: Context,
    classLoader: ClassLoader?,
) : Action {
    private val statusBar: Any?
    private val enterSplit: Method?
    private val exitSplit: Method?
    private val legacyToggle: Method?
    private val windowingMode: Method? =
        ActivityManager.RunningTaskInfo::class.java.findMethodUpward("getWindowingMode")

    val available: Boolean
        get() = statusBar != null && (enterSplit != null || legacyToggle != null)

    init {
        var service: Any? = null
        var enter: Method? = null
        var exit: Method? = null
        var legacy: Method? = null
        runCatching {
            val internalClass =
                Class.forName(
                    "com.android.server.statusbar.StatusBarManagerInternal",
                    false,
                    classLoader,
                )
            service =
                Class
                    .forName("com.android.server.LocalServices", false, classLoader)
                    .getMethod("getService", Class::class.java)
                    .invoke(null, internalClass)
            // A15+ has moveFocusedTaskToStageSplit
            // A14 only has enterStageSplitFromRunningApp
            enter =
                internalClass.findMethodUpward(
                    "moveFocusedTaskToStageSplit",
                    Int::class.javaPrimitiveType!!,
                    Boolean::class.javaPrimitiveType!!,
                ) ?: internalClass.findMethodUpward(
                    "enterStageSplitFromRunningApp",
                    Boolean::class.javaPrimitiveType!!,
                )
            // A15+ exits with moveFocusedTaskToFullscreen and A14 with goToFullscreenFromSplit
            exit =
                internalClass.findMethodUpward(
                    "moveFocusedTaskToFullscreen",
                    Int::class.javaPrimitiveType!!,
                ) ?: internalClass.findMethodUpward("goToFullscreenFromSplit")
            // A13 and older only have toggleSplitScreen which flips both ways on its own
            legacy = internalClass.findMethodUpward("toggleSplitScreen")
        }.onFailure {
            Logger.warn("split screen unavailable reason=resolve-failed", it)
        }
        statusBar = service
        enterSplit = enter
        exitSplit = exit
        legacyToggle = legacy
        val resolved = enterSplit ?: legacyToggle
        when {
            statusBar == null -> Logger.warn("split screen unavailable reason=no-service")
            resolved == null -> Logger.warn("split screen unavailable reason=no-method")
            else -> Logger.info("split screen resolved ${capability()}")
        }
    }

    fun capability(): String =
        "enter=${enterSplit?.signature()} exit=${exitSplit?.signature()} " +
            "legacy=${legacyToggle != null} probe=${windowingMode != null}"

    override fun execute() {
        runCatching {
            val service = statusBar ?: error("StatusBarManagerInternal unavailable")
            when {
                enterSplit == null && legacyToggle != null -> {
                    legacyToggle.invoke(service)
                }

                enterSplit == null -> {
                    error("no split screen method on this build")
                }

                exitSplit != null && isInSplit() -> {
                    when (exitSplit.parameterCount) {
                        // goToFullscreenFromSplit on A14 takes no display id
                        0 -> exitSplit.invoke(service)

                        else -> exitSplit.invoke(service, Display.DEFAULT_DISPLAY)
                    }
                }

                // two args means A15+ and one arg means A14
                enterSplit.parameterCount == 2 -> {
                    enterSplit.invoke(service, Display.DEFAULT_DISPLAY, true)
                }

                else -> {
                    enterSplit.invoke(service, true)
                }
            }
        }.onSuccess {
            Logger.info("split screen toggled")
        }.onFailure {
            Logger.error("split screen toggle failed", it)
        }
    }

    private fun isInSplit(): Boolean {
        val mode = windowingMode ?: return false
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false

        @Suppress("DEPRECATION")
        val task = am.getRunningTasks(1).firstOrNull() ?: return false
        return mode.invoke(task) == WINDOWING_MODE_MULTI_WINDOW
    }
}
