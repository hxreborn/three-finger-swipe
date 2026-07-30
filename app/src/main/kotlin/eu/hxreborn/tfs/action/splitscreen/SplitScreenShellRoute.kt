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

    @Volatile private var actions: Set<String> = emptySet()

    val resolved: Boolean
        get() = shellCommand != null

    val canEnter: Boolean
        get() = ENTER_SPLIT in actions

    val canExit: Boolean
        get() = EXIT_SPLIT in actions

    fun probe() {
        if (shellCommand == null) {
            Logger.warn("split screen shell route unavailable reason=no-shell-command")
            return
        }
        background {
            val help = send("help").orEmpty()
            actions =
                if (help.contains(SPLIT_SCREEN)) {
                    setOf(ENTER_SPLIT, EXIT_SPLIT).filterTo(mutableSetOf(), help::contains)
                } else {
                    emptySet()
                }
            Logger.info("split screen shell route actions=${actions.joinToString(",")}")
        }
    }

    fun enter(taskId: Int) =
        background {
            report(
                ENTER_SPLIT,
                send(
                    SPLIT_SCREEN,
                    ENTER_SPLIT,
                    taskId.toString(),
                    SIDE_STAGE_BOTTOM_OR_RIGHT.toString(),
                ),
            )
        }

    fun exit(taskId: Int) =
        background {
            report(EXIT_SPLIT, send(SPLIT_SCREEN, EXIT_SPLIT, taskId.toString()))
        }

    // the handler stays silent unless it refused the command
    private fun report(
        action: String,
        output: String?,
    ) {
        val refusal = output?.trim().orEmpty()
        if (refusal.isNotEmpty()) {
            Logger.warn("split screen shell refused action=$action reason=$refusal")
        }
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
