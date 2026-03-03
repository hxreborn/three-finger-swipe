package eu.hxreborn.tfs.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.util.shapeForPosition

@Composable
fun CaptureModeScreen(
    captureMode: CaptureMode,
    onCaptureModeChange: (CaptureMode) -> Unit,
    onBack: () -> Unit,
) {
    val defaultMode = CaptureMode.fromKey(Prefs.CAPTURE_MODE.default)

    SettingsDetailScaffold(
        title = stringResource(R.string.screen_capture_mode),
        onBack = onBack,
        actions = {
            IconButton(
                onClick = { onCaptureModeChange(defaultMode) },
                enabled = captureMode != defaultMode,
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = stringResource(R.string.action_reset))
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
        ) {
            CaptureModeCard(
                selected = captureMode == CaptureMode.REFLECTION,
                title = stringResource(R.string.pref_capture_mode_reflection_title),
                description = stringResource(R.string.pref_capture_mode_reflection_body),
                shape = shapeForPosition(2, 0),
                onClick = { onCaptureModeChange(CaptureMode.REFLECTION) },
            )

            Spacer(Modifier.height(2.dp))

            CaptureModeCard(
                selected = captureMode == CaptureMode.SYSRQ,
                title = stringResource(R.string.pref_capture_mode_sysrq_title),
                description = stringResource(R.string.pref_capture_mode_sysrq_body),
                shape = shapeForPosition(2, 1),
                onClick = { onCaptureModeChange(CaptureMode.SYSRQ) },
            )
        }
    }
}

@Composable
private fun CaptureModeCard(
    selected: Boolean,
    title: String,
    description: String,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CaptureModeScreenPreview() {
    AppTheme(useDynamicColor = false) {
        CaptureModeScreen(
            captureMode = CaptureMode.REFLECTION,
            onCaptureModeChange = {},
            onBack = {},
        )
    }
}
