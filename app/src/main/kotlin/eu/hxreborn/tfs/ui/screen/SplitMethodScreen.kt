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
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.SplitMethod
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.util.shapeForPosition

@Composable
fun SplitMethodScreen(
    splitMethod: SplitMethod,
    modifier: Modifier = Modifier,
    onSplitMethodChange: (SplitMethod) -> Unit,
    onBack: () -> Unit,
) {
    val defaultMethod = SplitMethod.fromKey(Prefs.SPLIT_METHOD.default)

    SettingsDetailScaffold(
        modifier = modifier,
        title = stringResource(R.string.screen_split_method),
        onBack = onBack,
        actions = {
            IconButton(
                onClick = { onSplitMethodChange(defaultMethod) },
                enabled = splitMethod != defaultMethod,
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = stringResource(R.string.action_reset))
            }
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
        ) {
            SplitMethodCard(
                selected = splitMethod == SplitMethod.NATIVE,
                title = stringResource(R.string.pref_split_method_native_title),
                description = stringResource(R.string.pref_split_method_native_body),
                shape = shapeForPosition(2, 0),
                onClick = { onSplitMethodChange(SplitMethod.NATIVE) },
            )

            Spacer(Modifier.height(2.dp))

            SplitMethodCard(
                selected = splitMethod == SplitMethod.WM_SHELL,
                title = stringResource(R.string.pref_split_method_shell_title),
                description = stringResource(R.string.pref_split_method_shell_body),
                shape = shapeForPosition(2, 1),
                onClick = { onSplitMethodChange(SplitMethod.WM_SHELL) },
            )
        }
    }
}

@Composable
private fun SplitMethodCard(
    selected: Boolean,
    title: String,
    description: String,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            modifier
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
private fun SplitMethodScreenPreview() {
    AppTheme(useDynamicColor = false) {
        SplitMethodScreen(
            splitMethod = SplitMethod.NATIVE,
            onSplitMethodChange = {},
            onBack = {},
        )
    }
}
