package eu.hxreborn.tfs.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Splitscreen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.AppPrefs
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.SplitMethod
import eu.hxreborn.tfs.ui.component.ChoiceButtonGroup
import eu.hxreborn.tfs.ui.component.PreferenceTitle
import eu.hxreborn.tfs.ui.component.SectionHeader
import eu.hxreborn.tfs.ui.component.SettingsRow
import eu.hxreborn.tfs.ui.component.groupedRow
import eu.hxreborn.tfs.ui.component.rowSurface
import eu.hxreborn.tfs.ui.navigation.Destination
import eu.hxreborn.tfs.ui.theme.AppTheme
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.Preference

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    state: AppPrefs,
    bottomPadding: Dp,
    onResetToDefaults: () -> Unit,
    onRestoreState: (AppPrefs) -> Unit,
    onCaptureModeChange: (CaptureMode) -> Unit,
    onSplitMethodChange: (SplitMethod) -> Unit,
    onNavigate: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val resetDone = stringResource(R.string.snackbar_reset_done)
    val undoLabel = stringResource(R.string.snackbar_undo)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = TopLevelInsets,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.screen_settings)) },
                windowInsets = TopBarInsets,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding.withScreenGutter(bottomPadding, horizontal = GroupedListGutter),
            ) {
                item(key = "header_advanced", contentType = "header") {
                    SectionHeader(stringResource(R.string.category_advanced))
                }
                item(key = "capture_mode", contentType = "choice") {
                    ChoiceRow(
                        title = stringResource(R.string.screen_capture_mode),
                        summary =
                            when (state.captureMode) {
                                CaptureMode.SYSTEM_API -> R.string.pref_capture_mode_reflection_body
                                CaptureMode.SYSRQ -> R.string.pref_capture_mode_sysrq_body
                            },
                        note = R.string.pref_capture_mode_reboot,
                        icon = { Icon(Icons.Outlined.CameraAlt, contentDescription = null) },
                        index = 0,
                        options = CaptureMode.entries,
                        selected = state.captureMode,
                        recommended = CaptureMode.SYSTEM_API,
                        onSelect = onCaptureModeChange,
                        label = {
                            when (it) {
                                CaptureMode.SYSTEM_API -> R.string.capture_mode_system_api
                                CaptureMode.SYSRQ -> R.string.capture_mode_sysrq
                            }
                        },
                    )
                }
                item(key = "split_method", contentType = "choice") {
                    ChoiceRow(
                        title = stringResource(R.string.screen_split_method),
                        summary =
                            when (state.splitMethod) {
                                SplitMethod.NATIVE -> R.string.pref_split_method_native_body
                                SplitMethod.WM_SHELL -> R.string.pref_split_method_shell_body
                            },
                        icon = { Icon(Icons.Outlined.Splitscreen, contentDescription = null) },
                        index = 1,
                        options = SplitMethod.entries,
                        selected = state.splitMethod,
                        recommended = SplitMethod.NATIVE,
                        onSelect = onSplitMethodChange,
                        label = {
                            when (it) {
                                SplitMethod.NATIVE -> R.string.split_method_native
                                SplitMethod.WM_SHELL -> R.string.split_method_shell
                            }
                        },
                    )
                }
                item(key = "reset", contentType = "row") {
                    SettingsRow(
                        title = stringResource(R.string.pref_reset_title),
                        summary = stringResource(R.string.pref_reset_summary),
                        icon = Icons.Outlined.RestartAlt,
                        index = 2,
                        count = ADVANCED_ROWS,
                        onClick = {
                            val snapshot = state
                            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                            onResetToDefaults()
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result =
                                    snackbarHostState.showSnackbar(
                                        message = resetDone,
                                        actionLabel = undoLabel,
                                    )
                                if (result == SnackbarResult.ActionPerformed) {
                                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                    onRestoreState(snapshot)
                                }
                            }
                        },
                    )
                }

                item(key = "header_about", contentType = "header") {
                    SectionHeader(stringResource(R.string.category_about))
                }
                item(key = "about", contentType = "row") {
                    SettingsRow(
                        title = stringResource(R.string.app_name),
                        summary = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        icon = Icons.Rounded.Info,
                        index = 0,
                        count = 1,
                        onClick = { onNavigate(Destination.About) },
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bottomPadding - 16.dp),
            )
        }
    }
}

private const val ADVANCED_ROWS = 3

@Composable
private fun <T> ChoiceRow(
    title: String,
    @StringRes summary: Int,
    icon: @Composable () -> Unit,
    index: Int,
    options: List<T>,
    selected: T,
    recommended: T,
    onSelect: (T) -> Unit,
    label: (T) -> Int,
    @StringRes note: Int? = null,
) {
    val haptics = LocalHapticFeedback.current
    Column(Modifier.groupedRow(rowSurface, index, ADVANCED_ROWS)) {
        Preference(
            icon = icon,
            title = { PreferenceTitle(title) },
            summary = {
                Column {
                    Text(stringResource(summary))
                    Text(
                        stringResource(R.string.pref_recommended, stringResource(label(recommended))),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    note?.let { Text(stringResource(it)) }
                }
            },
        )
        ChoiceButtonGroup(
            options = options,
            selected = selected,
            onSelect = {
                if (it != selected) {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onSelect(it)
                }
            },
            label = { stringResource(label(it)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AppTheme(useDynamicColor = false) {
        SettingsScreen(
            state = AppPrefs(),
            bottomPadding = 96.dp,
            onResetToDefaults = {},
            onRestoreState = {},
            onCaptureModeChange = {},
            onSplitMethodChange = {},
            onNavigate = {},
        )
    }
}
