package eu.hxreborn.tfs.util

import android.util.Log
import eu.hxreborn.tfs.ModuleConstants
import io.github.libxposed.api.XposedModule

object Logger {
    private val tag = ModuleConstants.LOG_TAG

    @Volatile
    private var module: XposedModule? = null

    @Volatile
    @PublishedApi
    internal var debugEnabled: Boolean = false

    fun attach(module: XposedModule) {
        this.module = module
    }

    fun log(message: String) {
        module?.log(Log.INFO, tag, message)
        Log.i(tag, message)
    }

    fun log(
        message: String,
        throwable: Throwable,
    ) {
        module?.log(Log.ERROR, tag, message, throwable)
        Log.e(tag, message, throwable)
    }

    inline fun logDebug(message: () -> String) {
        if (!debugEnabled) return
        log(message())
    }
}

fun log(message: String): Unit = Logger.log(message)

fun log(
    message: String,
    throwable: Throwable,
): Unit = Logger.log(message, throwable)

inline fun logDebug(message: () -> String): Unit = Logger.logDebug(message)
