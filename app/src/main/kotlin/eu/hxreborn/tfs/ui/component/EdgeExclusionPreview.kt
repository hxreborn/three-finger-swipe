package eu.hxreborn.tfs.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill

private const val REFERENCE_WIDTH_DP = 360f

@Composable
fun EdgeExclusionPreview(
    edgeExclusionDp: Int,
    modifier: Modifier = Modifier,
) {
    val bandColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)

    LottiePhoneMock(modifier = modifier) { screenRect ->
        val band = (edgeExclusionDp / REFERENCE_WIDTH_DP) * screenRect.width
        if (band > 0f) {
            val path =
                Path().apply {
                    // Left
                    addRect(Rect(screenRect.left, screenRect.top, screenRect.left + band, screenRect.bottom))
                    // Right
                    addRect(Rect(screenRect.right - band, screenRect.top, screenRect.right, screenRect.bottom))
                    // Top (between left and right bands to avoid overlap)
                    addRect(Rect(screenRect.left + band, screenRect.top, screenRect.right - band, screenRect.top + band))
                    // Bottom (between left and right bands to avoid overlap)
                    addRect(Rect(screenRect.left + band, screenRect.bottom - band, screenRect.right - band, screenRect.bottom))
                }
            drawPath(path, color = bandColor, style = Fill)
        }
    }
}
