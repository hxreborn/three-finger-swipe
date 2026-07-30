package eu.hxreborn.tfs.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import eu.hxreborn.tfs.R

@Composable
fun GestureIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary.toArgb()
    val dynamicProperties =
        rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(LottieProperty.COLOR, primary, "Shape Layer 4", "Ellipse 1", "Fill 1"),
            rememberLottieDynamicProperty(LottieProperty.COLOR, primary, "Shape Layer 5", "Ellipse 1", "Fill 1"),
            rememberLottieDynamicProperty(LottieProperty.COLOR, primary, "Shape Layer 6", "Ellipse 1", "Fill 1"),
        )
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.swipe_down_phone),
    )

    Box(modifier = modifier) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            dynamicProperties = dynamicProperties,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
        )
    }
}
