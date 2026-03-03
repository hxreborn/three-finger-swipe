package eu.hxreborn.tfs.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import eu.hxreborn.tfs.R

// Screen area within the Lottie asset, as fractions of the composition bounds
private const val SCREEN_LEFT = 0.305f
private const val SCREEN_TOP = 0.075f
private const val SCREEN_RIGHT = 0.670f
private const val SCREEN_BOTTOM = 0.929f
private const val SCREEN_CORNER_FRACTION = 0.04f

@Composable
fun LottiePhoneMock(
    modifier: Modifier = Modifier,
    onDrawScreen: DrawScope.(screenRect: Rect) -> Unit = {},
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.swipe_down_phone),
    )

    Box(modifier = modifier.fillMaxWidth().height(200.dp)) {
        LottieAnimation(
            composition = composition,
            progress = { 0f },
            modifier = Modifier.fillMaxSize(),
        )

        Canvas(Modifier.fillMaxSize()) {
            val comp = composition ?: return@Canvas
            val compBounds = comp.bounds
            val lottieAspect = compBounds.width().toFloat() / compBounds.height().toFloat()
            val boxAspect = size.width / size.height

            // Mirror ContentScale.Fit to find where the Lottie actually renders
            val lottieW: Float
            val lottieH: Float
            if (lottieAspect > boxAspect) {
                lottieW = size.width
                lottieH = size.width / lottieAspect
            } else {
                lottieH = size.height
                lottieW = size.height * lottieAspect
            }
            val lottieX = (size.width - lottieW) / 2f
            val lottieY = (size.height - lottieH) / 2f

            val screenLeft = lottieX + lottieW * SCREEN_LEFT
            val screenTop = lottieY + lottieH * SCREEN_TOP
            val screenWidth = lottieW * (SCREEN_RIGHT - SCREEN_LEFT)
            val screenHeight = lottieH * (SCREEN_BOTTOM - SCREEN_TOP)
            val cornerRadius = lottieW * SCREEN_CORNER_FRACTION

            val screenRect =
                Rect(
                    screenLeft,
                    screenTop,
                    screenLeft + screenWidth,
                    screenTop + screenHeight,
                )

            val screenPath =
                Path().apply {
                    addRoundRect(
                        RoundRect(rect = screenRect, cornerRadius = CornerRadius(cornerRadius)),
                    )
                }

            clipPath(screenPath) {
                onDrawScreen(screenRect)
            }
        }
    }
}
