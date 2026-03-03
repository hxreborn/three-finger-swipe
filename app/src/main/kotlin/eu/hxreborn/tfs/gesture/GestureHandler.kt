package eu.hxreborn.tfs.gesture

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.readOrDefault
import eu.hxreborn.tfs.util.log
import eu.hxreborn.tfs.util.logDebug
import kotlin.math.abs
import kotlin.math.hypot

class GestureHandler(
    context: Context,
    private val prefs: SharedPreferences?,
    private val config: GestureConfig = GestureConfig(),
    private val onTrigger: () -> Unit,
    private val onPilfer: () -> Unit = {},
) {
    private val displayMetrics = context.applicationContext.resources.displayMetrics
    private val smallestDimension = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels)

    private val edgeExclusionPx: Float
        get() = Prefs.EDGE_EXCLUSION_DP.readOrDefault(prefs) * displayMetrics.density

    private val swipeThresholdPx: Float
        get() = smallestDimension * Prefs.SWIPE_THRESHOLD_PCT.readOrDefault(prefs) / 100f

    private var state: GestureState = GestureState.Idle

    private var lastTriggerTime: Long = 0L

    fun onPointerEvent(event: MotionEvent) {
        if (!event.isTouchscreen) return
        if (Prefs.SELECTED_ACTION.readOrDefault(prefs) == ActionId.NO_ACTION.key) return

        state =
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> GestureState.Idle

                MotionEvent.ACTION_POINTER_DOWN -> handlePointerDown(event)

                MotionEvent.ACTION_MOVE -> handleMove(event)

                MotionEvent.ACTION_POINTER_UP,
                MotionEvent.ACTION_UP,
                -> handlePointerUp(event)

                MotionEvent.ACTION_CANCEL -> GestureState.Idle

                else -> state
            }
    }

    private fun handlePointerDown(event: MotionEvent): GestureState {
        if (event.pointerCount > config.requiredFingers) {
            return GestureState.Idle
        }
        if (event.pointerCount != config.requiredFingers) return state

        val points = event.points()
        val landingDuration = event.eventTime - event.downTime
        val landingWindowMs = Prefs.FINGER_LANDING_MS.readOrDefault(prefs).toLong()
        val exclusion = edgeExclusionPx

        logDebug {
            val positions =
                points.entries.joinToString { (id, p) ->
                    "#$id(${p.x.toInt()},${p.y.toInt()})"
                }
            "Gesture start: $positions landing=${landingDuration}ms " +
                "edge=${exclusion.toInt()}px threshold=${swipeThresholdPx.toInt()}px " +
                "cooldown=${Prefs.COOLDOWN_MS.readOrDefault(prefs)}ms"
        }

        return when {
            isInCooldown() -> {
                val cooldownMs = Prefs.COOLDOWN_MS.readOrDefault(prefs).toLong()
                val remaining =
                    (cooldownMs - (SystemClock.elapsedRealtime() - lastTriggerTime))
                        .coerceAtLeast(0L)
                logDebug {
                    "Rejected: cooldown active ${remaining}ms remaining of ${cooldownMs}ms"
                }
                GestureState.Idle
            }

            landingDuration > landingWindowMs -> {
                logDebug {
                    "Rejected: landing ${landingDuration}ms > window ${landingWindowMs}ms"
                }
                GestureState.Idle
            }

            !points.areGrouped() -> {
                logDebug {
                    val spread = points.values.maxSpread()
                    "Rejected: spread ${spread.toInt()}px > " +
                        "proximity ${config.startingProximityPx.toInt()}px"
                }
                GestureState.Idle
            }

            points.startsNearEdge() -> {
                logDebug {
                    val w = displayMetrics.widthPixels
                    val h = displayMetrics.heightPixels
                    val offending =
                        points.entries
                            .filter { (_, p) ->
                                p.x < exclusion || p.x > w - exclusion ||
                                    p.y < exclusion || p.y > h - exclusion
                            }.joinToString { (id, p) -> "#$id(${p.x.toInt()},${p.y.toInt()})" }
                    "Rejected: near edge $offending " +
                        "exclusion=${exclusion.toInt()}px screen=${w}x$h"
                }
                GestureState.Idle
            }

            else -> {
                onPilfer()
                GestureState.Tracking(
                    startPoints = points,
                    startTimeMs = event.eventTime,
                )
            }
        }
    }

    private fun handleMove(event: MotionEvent): GestureState {
        val tracking = state as? GestureState.Tracking ?: return state
        if (event.pointerCount != config.requiredFingers) return state

        return checkSwipeDown(tracking, event) ?: state
    }

    private fun handlePointerUp(event: MotionEvent): GestureState =
        when (val current = state) {
            is GestureState.Tracking -> {
                checkSwipeDown(current, event) ?: GestureState.Idle
            }

            is GestureState.Triggered -> {
                event.takeIf { it.pointerCount <= 1 }?.let { GestureState.Idle } ?: state
            }

            GestureState.Idle -> {
                state
            }
        }

    private fun checkSwipeDown(
        tracking: GestureState.Tracking,
        event: MotionEvent,
    ): GestureState.Triggered? {
        val now = SystemClock.elapsedRealtime()
        if (isInCooldown(now)) return null

        val currentPoints = event.points()
        val threshold = swipeThresholdPx
        val isValidSwipe =
            tracking.startPoints.all { (pointerId, startPoint) ->
                val currentPoint = currentPoints[pointerId] ?: return@all false
                val deltaY = currentPoint.y - startPoint.y
                val deltaX = currentPoint.x - startPoint.x

                deltaY >= threshold && abs(deltaX) <= abs(deltaY)
            }

        if (!isValidSwipe) return null

        val heldDuration = event.eventTime - tracking.startTimeMs
        lastTriggerTime = now
        log("Swipe fired: held=${heldDuration}ms threshold=${threshold.toInt()}px")
        logDebug {
            tracking.startPoints.entries.joinToString(" ") { (id, start) ->
                val cur = currentPoints[id]
                val dy = cur?.let { it.y - start.y }?.toInt() ?: 0
                val dx = cur?.let { it.x - start.x }?.toInt() ?: 0
                "#$id(dy=$dy dx=$dx)"
            }
        }
        onTrigger()
        return GestureState.Triggered
    }

    private fun isInCooldown(now: Long = SystemClock.elapsedRealtime()): Boolean {
        val cooldownMs = Prefs.COOLDOWN_MS.readOrDefault(prefs).toLong()
        return now - lastTriggerTime < cooldownMs
    }

    private fun Map<Int, PointF>.areGrouped(): Boolean =
        values.all { point ->
            values.any { other ->
                other !== point && point.distanceTo(other) <= config.startingProximityPx
            }
        }

    private fun Map<Int, PointF>.startsNearEdge(): Boolean {
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        return values.any { point ->
            point.x < edgeExclusionPx || point.x > screenWidth - edgeExclusionPx ||
                point.y < edgeExclusionPx ||
                point.y > screenHeight - edgeExclusionPx
        }
    }
}

private fun MotionEvent.points(): Map<Int, PointF> =
    buildMap(pointerCount) {
        repeat(pointerCount) { index ->
            put(getPointerId(index), PointF(getX(index), getY(index)))
        }
    }

private val MotionEvent.isTouchscreen: Boolean
    get() = source and InputDevice.SOURCE_TOUCHSCREEN == InputDevice.SOURCE_TOUCHSCREEN

private fun PointF.distanceTo(other: PointF): Float = hypot(x - other.x, y - other.y)

private fun Collection<PointF>.maxSpread(): Float =
    flatMap { a -> map { b -> a.distanceTo(b) } }.maxOrNull() ?: 0f
