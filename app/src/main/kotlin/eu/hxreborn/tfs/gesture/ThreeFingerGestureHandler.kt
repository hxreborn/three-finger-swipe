package eu.hxreborn.tfs.gesture

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.readOrDefault
import eu.hxreborn.tfs.util.log
import eu.hxreborn.tfs.util.logDebug
import kotlin.math.abs
import kotlin.math.hypot

// TODO: refactor
class ThreeFingerGestureHandler(
    context: Context,
    private val prefs: SharedPreferences?,
    private val config: GestureConfig = GestureConfig(),
    private val onSwipeDown: () -> Unit,
    private val onPilfer: () -> Unit = {},
) {
    private val displayMetrics = context.applicationContext.resources.displayMetrics
    private val edgeExclusionPx = config.edgeExclusionDp * displayMetrics.density
    private val swipeThresholdPx =
        minOf(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
        ) * config.swipeThresholdFraction

    private var state: GestureState = GestureState.Idle

    private var lastTriggerTime: Long = 0L

    fun onPointerEvent(event: MotionEvent) {
        if (!event.isTouchscreen) return
        if (!Prefs.SWIPE_ENABLED.readOrDefault(prefs)) return

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

        return when {
            landingDuration > config.fingerLandingWindowMs -> {
                logDebug { "Gesture rejected: fingers landed too slowly" }
                GestureState.Idle
            }

            !points.areGrouped() -> {
                logDebug { "Gesture rejected: fingers started too far apart" }
                GestureState.Idle
            }

            points.startsNearEdge() -> {
                logDebug { "Gesture rejected: fingers started near screen edge" }
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
        if (now - lastTriggerTime < config.cooldownMs) return null

        val currentPoints = event.points()
        val isValidSwipe =
            tracking.startPoints.all { (pointerId, startPoint) ->
                val currentPoint = currentPoints[pointerId] ?: return@all false
                val deltaY = currentPoint.y - startPoint.y
                val deltaX = currentPoint.x - startPoint.x

                deltaY >= swipeThresholdPx && abs(deltaX) <= abs(deltaY)
            }

        if (!isValidSwipe) return null

        val heldDuration = event.eventTime - tracking.startTimeMs
        lastTriggerTime = now
        log("Swipe fired: held=${heldDuration}ms threshold=${swipeThresholdPx.toInt()}px")
        onSwipeDown()
        return GestureState.Triggered
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
