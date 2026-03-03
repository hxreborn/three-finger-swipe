package eu.hxreborn.tfs.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.component.EdgeExclusionPreview
import eu.hxreborn.tfs.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun EdgeExclusionScreen(
    edgeExclusionDp: Int,
    onValueChange: (Int) -> Unit,
    onBack: () -> Unit,
) {
    var sliderValue by remember(edgeExclusionDp) {
        mutableFloatStateOf(edgeExclusionDp.toFloat())
    }

    SettingsDetailScaffold(
        title = stringResource(R.string.screen_edge_exclusion),
        onBack = onBack,
        actions = {
            IconButton(
                onClick = { onValueChange(Prefs.EDGE_EXCLUSION_DP.default) },
                enabled = edgeExclusionDp != Prefs.EDGE_EXCLUSION_DP.default,
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = stringResource(R.string.action_reset))
            }
        },
    ) {
        EdgeExclusionPreview(
            edgeExclusionDp = sliderValue.roundToInt(),
        )

        Spacer(Modifier.height(24.dp))

        val dpValue = sliderValue.roundToInt()
        val density = LocalDensity.current
        val px = (dpValue * density.density).roundToInt()

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Column {
                    Text(
                        text = stringResource(R.string.value_dp, dpValue),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = stringResource(R.string.value_approx_px, px),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                val range = Prefs.EDGE_EXCLUSION_DP.sliderRange!!
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onValueChange(sliderValue.roundToInt()) },
                    valueRange = range,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.pref_edge_exclusion_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EdgeExclusionScreenPreview() {
    AppTheme(useDynamicColor = false) {
        EdgeExclusionScreen(
            edgeExclusionDp = 50,
            onValueChange = {},
            onBack = {},
        )
    }
}
