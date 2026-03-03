package eu.hxreborn.tfs.xposed.hook

import android.view.MotionEvent
import eu.hxreborn.tfs.gesture.ThreeFingerSwipeHandler
import eu.hxreborn.tfs.util.log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

internal class PointerEventListenerProxy(
    private val gestureHandler: ThreeFingerSwipeHandler,
) : InvocationHandler {
    override fun invoke(
        proxy: Any?,
        method: Method,
        args: Array<out Any?>?,
    ): Any? =
        when (method.name) {
            "onPointerEvent" -> {
                (args?.firstOrNull() as? MotionEvent)?.let { event ->
                    runCatching { gestureHandler.onPointerEvent(event) }.onFailure {
                        log(
                            "onPointerEvent dispatch failed",
                            it,
                        )
                    }
                }
                null
            }

            "toString" -> {
                "TFS-PointerListener"
            }

            "hashCode" -> {
                System.identityHashCode(proxy)
            }

            "equals" -> {
                proxy === args?.firstOrNull()
            }

            else -> {
                null
            }
        }
}
