package eu.hxreborn.tfs.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.SplitMethod
import eu.hxreborn.tfs.ui.component.ChoiceRow
import eu.hxreborn.tfs.ui.component.ResetAction
import eu.hxreborn.tfs.ui.theme.AppTheme

@Composable
fun SplitMethodScreen(
    splitMethod: SplitMethod,
    onSplitMethodChange: (SplitMethod) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaultMethod = SplitMethod.fromKey(Prefs.SPLIT_METHOD.default)

    SettingsDetailScaffold(
        modifier = modifier,
        title = stringResource(R.string.screen_split_method),
        onBack = onBack,
        actions = {
            ResetAction(enabled = splitMethod != defaultMethod, onReset = { onSplitMethodChange(defaultMethod) })
        },
    ) {
        Column(Modifier.fillMaxWidth().selectableGroup()) {
            ChoiceRow(
                title = stringResource(R.string.pref_split_method_native_title),
                description = stringResource(R.string.pref_split_method_native_body),
                selected = splitMethod == SplitMethod.NATIVE,
                index = 0,
                count = 2,
                onSelect = { onSplitMethodChange(SplitMethod.NATIVE) },
            )
            ChoiceRow(
                title = stringResource(R.string.pref_split_method_shell_title),
                description = stringResource(R.string.pref_split_method_shell_body),
                selected = splitMethod == SplitMethod.WM_SHELL,
                index = 1,
                count = 2,
                onSelect = { onSplitMethodChange(SplitMethod.WM_SHELL) },
            )
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
