package eu.hxreborn.tfs.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

private const val BAND_START_FRACTION = 0.30f

@Composable
fun TriggerDistancePreview(
    thresholdPct: Int,
    modifier: Modifier = Modifier,
) {
    val bandColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val edgeColor = MaterialTheme.colorScheme.primary

    LottiePhoneMock(modifier = modifier) { screenRect ->
        val bandTop = screenRect.top + screenRect.height * BAND_START_FRACTION
        val bandHeight = (thresholdPct / 100f) * screenRect.height
        val bandBottom = bandTop + bandHeight
        val strokePx = 1.5.dp.toPx()

        drawRect(
            color = bandColor,
            topLeft = Offset(screenRect.left, bandTop),
            size = Size(screenRect.width, bandHeight),
        )

        drawLine(
            color = edgeColor,
            start = Offset(screenRect.left, bandTop),
            end = Offset(screenRect.right, bandTop),
            strokeWidth = strokePx,
        )

        if (bandBottom <= screenRect.bottom) {
            drawLine(
                color = edgeColor,
                start = Offset(screenRect.left, bandBottom),
                end = Offset(screenRect.right, bandBottom),
                strokeWidth = strokePx,
            )
        }
    }
}
