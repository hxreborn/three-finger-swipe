package eu.hxreborn.tfs.action.splitscreen

import android.os.ParcelFileDescriptor
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.findMethodUpward
import java.io.FileDescriptor
import java.lang.reflect.Method

private const val WM_SHELL = "wmshell-passthrough"
private const val SPLIT_SCREEN = "splitscreen"
private const val ENTER_SPLIT = "moveToSideStage"
private const val EXIT_SPLIT = "exitSplitScreen"
private const val SIDE_STAGE_BOTTOM_OR_RIGHT = 1

// WMShell owns split itself so this route needs neither the launcher nor desktop mode
internal class SplitScreenShellRoute(
    private val statusBar: Any,
    internalClass: Class<*>,
) {
    private val shellCommand: Method? =
        internalClass.findMethodUpward(
            "passThroughShellCommand",
            Array<String>::class.java,
            FileDescriptor::class.java,
        )

    @Volatile private var actions: Set<String>? = null

    val resolved: Boolean
        get() = shellCommand != null

    fun dispatch(
        taskId: Int,
        split: Boolean,
        fallback: () -> Unit,
    ) = background {
        val action = if (split) EXIT_SPLIT else ENTER_SPLIT
        if (action !in supportedActions()) {
            Logger.info("split screen shell route declined action=$action")
            fallback()
            return@background
        }
        val side = if (split) emptyArray() else arrayOf(SIDE_STAGE_BOTTOM_OR_RIGHT.toString())
        // the handler stays silent unless it refused the command
        val refusal = send(SPLIT_SCREEN, action, taskId.toString(), *side)?.trim().orEmpty()
        if (refusal.isEmpty()) {
            Logger.info("split screen dispatched branch=shell-$action task=$taskId split=$split")
        } else {
            Logger.warn("split screen shell refused action=$action reason=$refusal")
        }
    }

    // SystemUI is not bound yet at systemReady so an empty help means retry rather than absent
    private fun supportedActions(): Set<String> {
        actions?.let { return it }
        val help = send("help")
        if (help.isNullOrBlank()) {
            Logger.info("split screen shell route unresolved reason=no-status-bar")
            return emptySet()
        }
        val found =
            if (help.contains(SPLIT_SCREEN)) {
                setOf(ENTER_SPLIT, EXIT_SPLIT).filterTo(mutableSetOf(), help::contains)
            } else {
                emptySet()
            }
        actions = found
        Logger.info("split screen shell route actions=${found.joinToString(",")}")
        return found
    }

    // the pipe transfer waits on SystemUI so it stays off the system_server main thread
    private fun background(block: () -> Unit) {
        Thread {
            runCatching(block).onFailure { Logger.error("split screen shell route failed", it) }
        }.start()
    }

    private fun send(vararg args: String): String? =
        runCatching {
            val method = shellCommand ?: error("shell command unresolved")
            val (read, write) = ParcelFileDescriptor.createPipe()
            ParcelFileDescriptor.AutoCloseInputStream(read).use { output ->
                write.use { method.invoke(statusBar, arrayOf(WM_SHELL, *args), it.fileDescriptor) }
                output.readBytes().decodeToString()
            }
        }.onFailure {
            Logger.warn("split screen shell command failed args=${args.toList()}", it)
        }.getOrNull()
}
