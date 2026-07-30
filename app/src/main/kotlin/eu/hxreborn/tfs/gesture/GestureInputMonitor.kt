package eu.hxreborn.tfs.gesture

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.anyClassFromNames
import java.lang.reflect.Method
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val DISPOSE_TIMEOUT_MS = 2000L

// pilferPointers tells InputDispatcher to cancel touch delivery to the foreground app
internal object GestureInputMonitor {
    private var inputMonitor: Any? = null

    // InputEventReceiver closes its input channel once unreachable
    private var eventDrain: Any? = null
    private var pilferMethod: Method? = null
    private var monitorDisposeMethod: Method? = null
    private var drainDisposeMethod: Method? = null

    fun create(): Boolean {
        dispose()
        return runCatching {
            // InputManagerGlobal exists only from A14
            val inputManagerClass =
                javaClass.classLoader!!.anyClassFromNames(
                    "android.hardware.input.InputManagerGlobal",
                    "android.hardware.input.InputManager",
                )
            val inputManager = inputManagerClass.getMethod("getInstance").invoke(null)
            val monitor =
                inputManagerClass
                    .getMethod(
                        "monitorGestureInput",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                    ).invoke(inputManager, "tfs-gesture", 0)!!
            val channel = monitor.javaClass.getMethod("getInputChannel").invoke(monitor)
            val channelClass = Class.forName("android.view.InputChannel")
            val receiverClass = Class.forName("android.view.BatchedInputEventReceiver")
            val drain =
                receiverClass
                    .getDeclaredConstructor(
                        channelClass,
                        Looper::class.java,
                        Choreographer::class.java,
                    ).newInstance(channel, Looper.getMainLooper(), Choreographer.getInstance())
            inputMonitor = monitor
            eventDrain = drain
            pilferMethod = monitor.javaClass.getMethod("pilferPointers")
            monitorDisposeMethod = monitor.javaClass.getMethod("dispose")
            drainDisposeMethod = drain.javaClass.getMethod("dispose")
            Logger.info("input monitor created source=${inputManagerClass.simpleName}")
            true
        }.onFailure {
            dispose()
            Logger.warn("pilfer unavailable reason=monitor-create-failed", it)
        }.getOrDefault(false)
    }

    fun pilferPointers() {
        val monitor = inputMonitor ?: return
        val pilfer =
            pilferMethod ?: run {
                Logger.warn("pilfer skipped reason=no-method")
                return
            }
        runCatching { pilfer.invoke(monitor) }
            .onSuccess { Logger.debug { "pointers pilfered" } }
            .onFailure { Logger.warn("pointer pilfer failed", it) }
    }

    // InputEventReceiver.dispose asserts its own Looper and hot reload retires us off a Binder thread
    fun dispose() {
        val main = Looper.getMainLooper()
        if (Looper.myLooper() == main) {
            disposeOnMain()
            return
        }
        val done = CountDownLatch(1)
        val posted =
            Handler(main).post {
                try {
                    disposeOnMain()
                } finally {
                    done.countDown()
                }
            }
        if (!posted) {
            Logger.warn("input monitor dispose skipped reason=main-handler-rejected")
            return
        }
        val finished =
            try {
                done.await(DISPOSE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                Logger.warn("input monitor dispose unfinished reason=interrupted", e)
                return
            }
        if (!finished) {
            Logger.warn("input monitor dispose unfinished reason=await-timeout")
        }
    }

    private fun disposeOnMain() {
        eventDrain?.let { drain ->
            runCatching {
                (drainDisposeMethod ?: drain.javaClass.getMethod("dispose")).invoke(drain)
            }.onFailure { Logger.warn("input receiver dispose failed", it) }
        }
        inputMonitor?.let { monitor ->
            runCatching {
                (monitorDisposeMethod ?: monitor.javaClass.getMethod("dispose")).invoke(monitor)
            }.onFailure { Logger.warn("input monitor dispose failed", it) }
        }
        eventDrain = null
        inputMonitor = null
        pilferMethod = null
        monitorDisposeMethod = null
        drainDisposeMethod = null
    }
}
