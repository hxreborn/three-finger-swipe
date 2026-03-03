package eu.hxreborn.tfs.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.component.TriggerDistancePreview
import eu.hxreborn.tfs.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun TriggerDistanceScreen(
    swipeThresholdPct: Int,
    onValueChange: (Int) -> Unit,
    onBack: () -> Unit,
) {
    var sliderValue by remember(swipeThresholdPct) {
        mutableFloatStateOf(swipeThresholdPct.toFloat())
    }

    SettingsDetailScaffold(
        title = stringResource(R.string.screen_trigger_distance),
        onBack = onBack,
        actions = {
            IconButton(
                onClick = { onValueChange(Prefs.SWIPE_THRESHOLD_PCT.default) },
                enabled = swipeThresholdPct != Prefs.SWIPE_THRESHOLD_PCT.default,
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = stringResource(R.string.action_reset))
            }
        },
    ) {
        TriggerDistancePreview(
            thresholdPct = sliderValue.roundToInt(),
        )

        Spacer(Modifier.height(24.dp))

        val configuration = LocalConfiguration.current
        val shortestDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
        val pct = sliderValue.roundToInt()
        val thresholdDp = shortestDp * pct / 100

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stringResource(R.string.value_percent, pct),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.value_approx_dp, thresholdDp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                val range = Prefs.SWIPE_THRESHOLD_PCT.sliderRange!!
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onValueChange(sliderValue.roundToInt()) },
                    valueRange = range,
                    steps = Prefs.SWIPE_THRESHOLD_PCT.sliderSteps,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.pref_swipe_threshold_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TriggerDistanceScreenPreview() {
    AppTheme(useDynamicColor = false) {
        TriggerDistanceScreen(
            swipeThresholdPct = 14,
            onValueChange = {},
            onBack = {},
        )
    }
}
