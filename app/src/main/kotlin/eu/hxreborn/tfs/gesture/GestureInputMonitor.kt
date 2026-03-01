package eu.hxreborn.tfs.gesture

import android.os.Looper
import android.view.Choreographer
import eu.hxreborn.tfs.gesture.GestureInputMonitor.pilferPointers
import eu.hxreborn.tfs.util.log

/**
 * Reflection-based [android.view.InputMonitor] for pointer pilfering.
 *
 * When three fingers land, [pilferPointers] tells InputDispatcher to cancel
 * touch delivery to the foreground app, preventing scrolling and pull-down
 * during the gesture.
 */
internal object GestureInputMonitor {
    private var inputMonitor: Any? = null

    // prevent GC of BatchedInputEventReceiver, which would close the input channel
    @Suppress("unused")
    private var eventDrain: Any? = null

    fun create(): Boolean =
        runCatching {
            val imgClass = Class.forName("android.hardware.input.InputManagerGlobal")
            val img = imgClass.getMethod("getInstance").invoke(null)
            val monitor =
                imgClass
                    .getMethod(
                        "monitorGestureInput",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                    ).invoke(img, "tfs-gesture", 0)!!
            val channel = monitor.javaClass.getMethod("getInputChannel").invoke(monitor)
            val channelClass = Class.forName("android.view.InputChannel")
            val receiverClass = Class.forName("android.view.BatchedInputEventReceiver")
            eventDrain =
                receiverClass
                    .getDeclaredConstructor(
                        channelClass,
                        Looper::class.java,
                        Choreographer::class.java,
                    ).newInstance(channel, Looper.getMainLooper(), Choreographer.getInstance())
            inputMonitor = monitor
            log("GestureInputMonitor created")
            true
        }.onFailure {
            log("GestureInputMonitor creation failed (pilfer unavailable)", it)
        }.getOrDefault(false)

    fun pilferPointers() {
        val monitor = inputMonitor ?: return
        runCatching {
            monitor.javaClass.getMethod("pilferPointers").invoke(monitor)
            log("Pointers pilfered")
        }.onFailure {
            log("pilferPointers failed", it)
        }
    }
}
