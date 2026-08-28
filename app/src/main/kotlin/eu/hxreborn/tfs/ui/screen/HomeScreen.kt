package eu.hxreborn.tfs.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AppBlocking
import androidx.compose.material.icons.outlined.BorderOuter
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Splitscreen
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.prefs.AppPrefs
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.SplitMethod
import eu.hxreborn.tfs.ui.component.GestureIllustration
import eu.hxreborn.tfs.ui.navigation.Destination
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.util.shapeForPosition
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.preferenceTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: AppPrefs,
    modifier: Modifier = Modifier,
    onActionChange: (ActionId) -> Unit,
    onFingerLandingChange: (Int) -> Unit,
    onCooldownChange: (Int) -> Unit,
    onResetToDefaults: () -> Unit,
    onRestoreState: (AppPrefs) -> Unit,
    onNavigate: (Destination) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resetDone = stringResource(R.string.snackbar_reset_done)
    val undoLabel = stringResource(R.string.snackbar_undo)
    val showExpandedTitle by remember(scrollBehavior) {
        derivedStateOf { scrollBehavior.state.collapsedFraction < 0.5f }
    }
    var showActionDialog by remember { mutableStateOf(false) }

    if (showActionDialog) {
        ActionPickerDialog(
            selectedAction = state.selectedAction,
            onActionChange = {
                onActionChange(it)
                showActionDialog = false
            },
            onDismiss = { showActionDialog = false },
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 2,
                        style =
                            if (showExpandedTitle) {
                                MaterialTheme.typography.headlineLarge
                            } else {
                                MaterialTheme.typography.titleLarge
                            },
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val surface = MaterialTheme.colorScheme.surfaceVariant

        ProvidePreferenceLocals(
            theme =
                preferenceTheme(
                    titleTextStyle = MaterialTheme.typography.titleMedium,
                ),
        ) {
            val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = navBarsPadding.calculateBottomPadding() + 16.dp,
                    ),
            ) {
                item(key = "illustration", contentType = "illustration") {
                    GestureIllustration(
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }

                preferenceCategory(
                    key = "category_gestures",
                    title = {
                        Text(
                            text = stringResource(R.string.category_swipe_down_action),
                            modifier = Modifier.semantics { heading() },
                        )
                    },
                )

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(1, 0)),
                    key = "action_picker",
                    icon = { Icon(Icons.Outlined.Gesture, contentDescription = null) },
                    title = { Text(stringResource(R.string.pref_swipe_down_action_title)) },
                    summary = { Text(stringResource(state.selectedAction.labelRes())) },
                    onClick = { showActionDialog = true },
                )

                preferenceCategory(
                    key = "category_settings",
                    title = {
                        Text(
                            text = stringResource(R.string.category_settings),
                            modifier = Modifier.semantics { heading() },
                        )
                    },
                )

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(5, 0)),
                    key = "nav_capture_mode",
                    icon = { Icon(Icons.Outlined.CameraAlt, contentDescription = null) },
                    title = { Text(stringResource(R.string.screen_capture_mode)) },
                    summary = {
                        Text(
                            when (state.captureMode) {
                                CaptureMode.SYSTEM_API -> {
                                    stringResource(R.string.pref_capture_mode_reflection_title)
                                }

                                CaptureMode.SYSRQ -> {
                                    stringResource(R.string.pref_capture_mode_sysrq_title)
                                }
                            },
                        )
                    },
                    onClick = { onNavigate(Destination.CaptureMode) },
                )

                preferenceSpacer("spacer_split_method")

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(5, 1)),
                    key = "nav_split_method",
                    icon = { Icon(Icons.Outlined.Splitscreen, contentDescription = null) },
                    title = { Text(stringResource(R.string.screen_split_method)) },
                    summary = {
                        Text(
                            when (state.splitMethod) {
                                SplitMethod.NATIVE -> {
                                    stringResource(R.string.pref_split_method_native_title)
                                }

                                SplitMethod.WM_SHELL -> {
                                    stringResource(R.string.pref_split_method_shell_title)
                                }
                            },
                        )
                    },
                    onClick = { onNavigate(Destination.SplitMethod) },
                )

                preferenceSpacer("spacer_capture")

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(5, 2)),
                    key = "nav_trigger_distance",
                    icon = { Icon(Icons.Outlined.SwipeDown, contentDescription = null) },
                    title = { Text(stringResource(R.string.screen_trigger_distance)) },
                    summary = {
                        Text(stringResource(R.string.value_percent, state.swipeThresholdPct))
                    },
                    onClick = { onNavigate(Destination.TriggerDistance) },
                )

                preferenceSpacer("spacer_sensitivity")

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(5, 3)),
                    key = "nav_edge_exclusion",
                    icon = { Icon(Icons.Outlined.BorderOuter, contentDescription = null) },
                    title = { Text(stringResource(R.string.screen_edge_exclusion)) },
                    summary = {
                        Text(stringResource(R.string.value_dp, state.edgeExclusionDp))
                    },
                    onClick = { onNavigate(Destination.EdgeExclusion) },
                )

                preferenceSpacer("spacer_edge_exclusion")

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(5, 4)),
                    key = "nav_app_filter",
                    icon = { Icon(Icons.Outlined.AppBlocking, contentDescription = null) },
                    title = { Text(stringResource(R.string.screen_app_filter)) },
                    summary = {
                        Text(
                            if (state.filteredApps.isEmpty()) {
                                stringResource(R.string.pref_app_filter_summary_off)
                            } else {
                                pluralStringResource(
                                    state.appFilterMode.summaryRes(),
                                    state.filteredApps.size,
                                    state.filteredApps.size,
                                )
                            },
                        )
                    },
                    onClick = { onNavigate(Destination.AppFilter) },
                )

                preferenceCategory(
                    key = "category_timing",
                    title = {
                        Text(
                            text = stringResource(R.string.category_timing),
                            modifier = Modifier.semantics { heading() },
                        )
                    },
                )

                timingPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(2, 0)),
                    key = Prefs.FINGER_LANDING_MS.key,
                    value = state.fingerLandingMs,
                    defaultValue = Prefs.FINGER_LANDING_MS.default,
                    valueRange = Prefs.FINGER_LANDING_MS.sliderRange!!,
                    snapInterval = Prefs.FINGER_LANDING_MS.step ?: 1,
                    icon = Icons.Outlined.HourglassEmpty,
                    titleRes = R.string.pref_finger_landing_title,
                    summaryRes = R.string.pref_finger_landing_summary,
                    onValueChange = onFingerLandingChange,
                )

                preferenceSpacer("spacer_landing")

                timingPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(2, 1)),
                    key = Prefs.COOLDOWN_MS.key,
                    value = state.cooldownMs,
                    defaultValue = Prefs.COOLDOWN_MS.default,
                    valueRange = Prefs.COOLDOWN_MS.sliderRange!!,
                    snapInterval = Prefs.COOLDOWN_MS.step ?: 1,
                    icon = Icons.Outlined.Timer,
                    titleRes = R.string.pref_cooldown_title,
                    summaryRes = R.string.pref_cooldown_summary,
                    onValueChange = onCooldownChange,
                )

                preferenceCategory(
                    key = "category_advanced",
                    title = {
                        Text(
                            text = stringResource(R.string.category_advanced),
                            modifier = Modifier.semantics { heading() },
                        )
                    },
                )

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(1, 0)),
                    key = "reset_to_defaults",
                    icon = { Icon(Icons.Outlined.RestartAlt, contentDescription = null) },
                    title = { Text(stringResource(R.string.pref_reset_title)) },
                    summary = { Text(stringResource(R.string.pref_reset_summary)) },
                    onClick = {
                        val snapshot = state
                        onResetToDefaults()
                        scope.launch {
                            val result =
                                snackbarHostState.showSnackbar(
                                    message = resetDone,
                                    actionLabel = undoLabel,
                                )
                            if (result == SnackbarResult.ActionPerformed) {
                                onRestoreState(snapshot)
                            }
                        }
                    },
                )

                preferenceCategory(
                    key = "category_about",
                    title = {
                        Text(
                            text = stringResource(R.string.category_about),
                            modifier = Modifier.semantics { heading() },
                        )
                    },
                )

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(1, 0)),
                    key = "nav_about",
                    icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    title = { Text(stringResource(R.string.category_about)) },
                    summary = { Text("v${BuildConfig.VERSION_NAME}") },
                    onClick = { onNavigate(Destination.About) },
                )
            }
        }
    }
}

private fun LazyListScope.preferenceSpacer(key: String) {
    item(key = key, contentType = "spacer") { Spacer(Modifier.height(2.dp)) }
}

private fun LazyListScope.timingPreference(
    key: String,
    value: Int,
    defaultValue: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    snapInterval: Int = 1,
    @StringRes titleRes: Int,
    @StringRes summaryRes: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit,
) {
    item(key = key, contentType = "TimingPreference") {
        var sliderValue by remember(value) { mutableFloatStateOf(value.toFloat()) }
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .widthIn(min = 56.dp)
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(icon, contentDescription = null)
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(top = 16.dp, bottom = 16.dp),
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(summaryRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = snap(it, snapInterval).toFloat()
                        },
                        onValueChangeFinished = {
                            onValueChange(sliderValue.roundToInt())
                        },
                        valueRange = valueRange,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.value_ms, sliderValue.roundToInt()),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            IconButton(
                onClick = { onValueChange(defaultValue) },
                enabled = value != defaultValue,
            ) {
                Icon(
                    Icons.Outlined.RestartAlt,
                    contentDescription = stringResource(R.string.action_reset),
                )
            }
        }
    }
}

private fun LazyListScope.navigablePreference(
    key: String,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    item(key = key, contentType = "Preference") {
        Preference(
            title = { title() },
            modifier = modifier,
            icon = icon,
            summary = summary,
            onClick = onClick,
        )
    }
}

private fun Modifier.preferenceCard(
    color: Color,
    shape: Shape,
): Modifier =
    padding(horizontal = 8.dp)
        .background(color = color, shape = shape)
        .clip(shape)

private fun snap(
    value: Float,
    interval: Int,
): Int {
    val raw = value.roundToInt()
    if (interval <= 1) return raw
    return ((raw + interval / 2) / interval) * interval
}

@Composable
private fun ActionPickerDialog(
    selectedAction: ActionId,
    modifier: Modifier = Modifier,
    onActionChange: (ActionId) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pref_swipe_down_action_title)) },
        text = {
            Column(
                Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState()),
            ) {
                ActionId.entries.forEach { action ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .selectable(
                                    selected = selectedAction == action,
                                    onClick = { onActionChange(action) },
                                    role = Role.RadioButton,
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selectedAction == action, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(action.labelRes()))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@androidx.annotation.StringRes
private fun ActionId.labelRes(): Int =
    when (this) {
        ActionId.NO_ACTION -> R.string.action_no_action
        ActionId.SCREENSHOT -> R.string.action_screenshot
        ActionId.SCREEN_OFF -> R.string.action_screen_off
        ActionId.TOGGLE_FLASHLIGHT -> R.string.action_toggle_flashlight
        ActionId.RINGER_MODE -> R.string.action_ringer_mode
        ActionId.TOGGLE_SPLIT_SCREEN -> R.string.action_toggle_split_screen
    }

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme(useDynamicColor = false) {
        HomeScreen(
            state = AppPrefs(),
            onActionChange = {},
            onFingerLandingChange = {},
            onCooldownChange = {},
            onResetToDefaults = {},
            onRestoreState = {},
            onNavigate = {},
        )
    }
}
