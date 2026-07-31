package eu.hxreborn.tfs.action.splitscreen

import android.app.ActivityManager
import android.content.Context
import android.view.Display
import eu.hxreborn.tfs.action.Action
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.findMethodUpward
import eu.hxreborn.tfs.util.signature
import java.lang.reflect.Method

private const val WINDOWING_MODE_FULLSCREEN = 1
private const val WINDOWING_MODE_MULTI_WINDOW = 6
private const val ACTIVITY_TYPE_STANDARD = 1

class ToggleSplitScreenAction(
    private val context: Context,
    classLoader: ClassLoader?,
) : Action {
    private val statusBar: Any?
    private val enterSplit: Method?
    private val exitSplit: Method?
    private val legacyToggle: Method?
    private val shellRoute: SplitScreenShellRoute?
    private val windowingMode: Method? =
        ActivityManager.RunningTaskInfo::class.java.findMethodUpward("getWindowingMode")
    private val activityType: Method? =
        ActivityManager.RunningTaskInfo::class.java.findMethodUpward("getActivityType")

    val available: Boolean
        get() =
            statusBar != null &&
                (enterSplit != null || legacyToggle != null || shellRoute?.resolved == true)

    init {
        var service: Any? = null
        var enter: Method? = null
        var exit: Method? = null
        var legacy: Method? = null
        var route: SplitScreenShellRoute? = null
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
            route = service?.let { SplitScreenShellRoute(it, internalClass) }
        }.onFailure {
            Logger.warn("split screen unavailable reason=resolve-failed", it)
        }
        statusBar = service
        enterSplit = enter
        exitSplit = exit
        legacyToggle = legacy
        shellRoute = route
        when {
            statusBar == null -> Logger.warn("split screen unavailable reason=no-service")
            !available -> Logger.warn("split screen unavailable reason=no-method")
            else -> Logger.info("split screen resolved ${capability()}")
        }
    }

    fun capability(): String =
        "enter=${enterSplit?.signature()} exit=${exitSplit?.signature()} " +
            "legacy=${legacyToggle != null} probe=${windowingMode != null} " +
            "shell=${shellRoute?.resolved == true}"

    override fun execute() {
        val task = focusedTask()
        val split = task != null && isSplit(task)
        if (shellRoute?.resolved == true && task != null && shellSafe(task, split)) {
            shellRoute.dispatch(task.taskId, split) { dispatchDirect(task, split) }
            return
        }
        dispatchDirect(task, split)
    }

    private fun dispatchDirect(
        task: ActivityManager.RunningTaskInfo?,
        split: Boolean,
    ) {
        runCatching {
            val service = statusBar ?: error("StatusBarManagerInternal unavailable")
            when {
                enterSplit == null && legacyToggle != null -> {
                    legacyToggle.invoke(service)
                    "legacy"
                }

                enterSplit == null -> {
                    error("no split screen method on this build")
                }

                exitSplit != null && split -> {
                    when (exitSplit.parameterCount) {
                        // goToFullscreenFromSplit on A14 takes no display id
                        0 -> exitSplit.invoke(service)

                        else -> exitSplit.invoke(service, Display.DEFAULT_DISPLAY)
                    }
                    "exit"
                }

                // two args means A15+ and one arg means A14
                enterSplit.parameterCount == 2 -> {
                    enterSplit.invoke(service, Display.DEFAULT_DISPLAY, true)
                    "enter"
                }

                else -> {
                    enterSplit.invoke(service, true)
                    "enter-legacy"
                }
            }
        }.onSuccess {
            Logger.info("split screen dispatched branch=$it task=${task?.taskId} split=$split")
        }.onFailure {
            Logger.error("split screen toggle failed task=${task?.taskId} split=$split", it)
        }
    }

    // moveToStage throws on the shell thread for a task WMShell does not own or already split
    private fun shellSafe(
        task: ActivityManager.RunningTaskInfo,
        split: Boolean,
    ): Boolean {
        val type = intOf(activityType, task)
        val mode = intOf(windowingMode, task)
        val wanted = if (split) WINDOWING_MODE_MULTI_WINDOW else WINDOWING_MODE_FULLSCREEN
        val safe = type == ACTIVITY_TYPE_STANDARD && mode == wanted
        if (!safe) {
            Logger.info("split screen shell skipped task=${task.taskId} type=$type mode=$mode")
        }
        return safe
    }

    private fun isSplit(task: ActivityManager.RunningTaskInfo): Boolean =
        intOf(windowingMode, task) == WINDOWING_MODE_MULTI_WINDOW

    private fun intOf(
        getter: Method?,
        task: ActivityManager.RunningTaskInfo,
    ): Int? = runCatching { getter?.invoke(task) as? Int }.getOrNull()

    private fun focusedTask(): ActivityManager.RunningTaskInfo? {
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return null

        @Suppress("DEPRECATION")
        return runCatching { am.getRunningTasks(1).firstOrNull() }
            .onFailure { Logger.warn("split screen focused task unavailable", it) }
            .getOrNull()
    }
}
