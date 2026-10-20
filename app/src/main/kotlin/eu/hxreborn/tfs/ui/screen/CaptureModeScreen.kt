package eu.hxreborn.tfs.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.component.ChoiceRow
import eu.hxreborn.tfs.ui.component.ResetAction
import eu.hxreborn.tfs.ui.theme.AppTheme

@Composable
fun CaptureModeScreen(
    captureMode: CaptureMode,
    onCaptureModeChange: (CaptureMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaultMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.default)

    SettingsDetailScaffold(
        modifier = modifier,
        title = stringResource(R.string.screen_capture_mode),
        onBack = onBack,
        actions = {
            ResetAction(enabled = captureMode != defaultMode, onReset = { onCaptureModeChange(defaultMode) })
        },
    ) {
        Column(Modifier.fillMaxWidth().selectableGroup()) {
            ChoiceRow(
                title = stringResource(R.string.pref_capture_mode_reflection_title),
                description = stringResource(R.string.pref_capture_mode_reflection_body),
                selected = captureMode == CaptureMode.SYSTEM_API,
                index = 0,
                count = 2,
                onSelect = { onCaptureModeChange(CaptureMode.SYSTEM_API) },
            )
            ChoiceRow(
                title = stringResource(R.string.pref_capture_mode_sysrq_title),
                description = stringResource(R.string.pref_capture_mode_sysrq_body),
                selected = captureMode == CaptureMode.SYSRQ,
                index = 1,
                count = 2,
                onSelect = { onCaptureModeChange(CaptureMode.SYSRQ) },
            )
        }

        Text(
            text = stringResource(R.string.pref_capture_mode_reboot),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CaptureModeScreenPreview() {
    AppTheme(useDynamicColor = false) {
        CaptureModeScreen(
            captureMode = CaptureMode.SYSTEM_API,
            onCaptureModeChange = {},
            onBack = {},
        )
    }
}
