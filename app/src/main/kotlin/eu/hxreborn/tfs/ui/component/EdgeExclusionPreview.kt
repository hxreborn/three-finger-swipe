package eu.hxreborn.tfs.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

private const val REFERENCE_WIDTH_DP = 360f

@Composable
fun EdgeExclusionPreview(
    edgeExclusionDp: Int,
    modifier: Modifier = Modifier,
) {
    val bandColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

    LottiePhoneMock(modifier = modifier) { screenRect ->
        val band = ((edgeExclusionDp / REFERENCE_WIDTH_DP) * screenRect.width).coerceIn(0f, screenRect.width / 2f)
        if (band > 0f) {
            val centerWidth = screenRect.width - 2f * band
            drawRect(
                color = bandColor,
                topLeft = Offset(screenRect.left, screenRect.top),
                size = Size(band, screenRect.height),
            )
            drawRect(
                color = bandColor,
                topLeft = Offset(screenRect.right - band, screenRect.top),
                size = Size(band, screenRect.height),
            )
            drawRect(
                color = bandColor,
                topLeft = Offset(screenRect.left + band, screenRect.top),
                size = Size(centerWidth, band),
            )
            drawRect(
                color = bandColor,
                topLeft = Offset(screenRect.left + band, screenRect.bottom - band),
                size = Size(centerWidth, band),
            )
        }
    }
}
