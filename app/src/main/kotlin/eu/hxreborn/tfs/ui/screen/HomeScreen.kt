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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BorderOuter
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.PrefsState
import eu.hxreborn.tfs.ui.component.GestureIllustration
import eu.hxreborn.tfs.ui.navigation.Destination
import eu.hxreborn.tfs.ui.util.shapeForPosition
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.preferenceTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: PrefsState,
    pendingReboot: Boolean,
    onSwipeEnabledChange: (Boolean) -> Unit,
    onFingerLandingChange: (Int) -> Unit,
    onCooldownChange: (Int) -> Unit,
    onDebugLogsChange: (Boolean) -> Unit,
    onResetToDefaults: () -> Unit,
    onRestoreState: (PrefsState) -> Unit,
    onNavigate: (Destination) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resetDone = stringResource(R.string.snackbar_reset_done)
    val undoLabel = stringResource(R.string.snackbar_undo)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.app_name), maxLines = 2) },
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
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
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

                // Screenshot gesture

                preferenceCategory(
                    key = "category_gestures",
                    title = { Text(stringResource(R.string.category_screenshot_gesture)) },
                )

                switchPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(1, 0)),
                    key = Prefs.SWIPE_ENABLED.key,
                    value = state.swipeEnabled,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Gesture,
                            contentDescription = null,
                        )
                    },
                    title = { Text(stringResource(R.string.pref_swipe_title)) },
                    summary = { Text(stringResource(R.string.pref_swipe_summary)) },
                    onValueChange = onSwipeEnabledChange,
                )

                // Settings (navigable rows)

                preferenceCategory(
                    key = "category_settings",
                    title = { Text(stringResource(R.string.category_settings)) },
                )

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(3, 0)),
                    key = "nav_capture_mode",
                    icon = { Icon(Icons.Outlined.CameraAlt, contentDescription = null) },
                    title = { Text(stringResource(R.string.screen_capture_mode)) },
                    summary = {
                        Text(
                            when (state.captureMode) {
                                CaptureMode.REFLECTION -> {
                                    stringResource(R.string.pref_capture_mode_reflection)
                                }

                                CaptureMode.SYSRQ -> {
                                    stringResource(R.string.pref_capture_mode_sysrq)
                                }
                            },
                        )
                    },
                    onClick = { onNavigate(Destination.CaptureMode) },
                )

                preferenceSpacer("spacer_capture")

                navigablePreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(3, 1)),
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
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(3, 2)),
                    key = "nav_edge_exclusion",
                    icon = { Icon(Icons.Outlined.BorderOuter, contentDescription = null) },
                    title = { Text(stringResource(R.string.screen_edge_exclusion)) },
                    summary = {
                        Text(stringResource(R.string.value_dp, state.edgeExclusionDp))
                    },
                    onClick = { onNavigate(Destination.EdgeExclusion) },
                )

                // Timing (inline sliders with 50ms steps)

                preferenceCategory(
                    key = "category_timing",
                    title = { Text(stringResource(R.string.category_timing)) },
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

                // Advanced

                preferenceCategory(
                    key = "category_advanced",
                    title = { Text(stringResource(R.string.category_advanced)) },
                )

                switchPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(2, 0)),
                    key = Prefs.DEBUG_LOGS.key,
                    value = state.debugLogs,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.BugReport,
                            contentDescription = null,
                        )
                    },
                    title = { Text(stringResource(R.string.pref_debug_title)) },
                    summary = { Text(stringResource(R.string.pref_debug_summary)) },
                    onValueChange = onDebugLogsChange,
                )

                preferenceSpacer("spacer_debug")

                preference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(2, 1)),
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
            }
        }
    }
}

// Spacer between grouped preference items

private fun LazyListScope.preferenceSpacer(key: String) {
    item(key = key, contentType = "spacer") { Spacer(Modifier.height(2.dp)) }
}

// Timing preference with icon, title, reset button, description, and slider

private fun LazyListScope.timingPreference(
    key: String,
    value: Int,
    defaultValue: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    snapInterval: Int = 1,
    @StringRes titleRes: Int,
    @StringRes summaryRes: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth(),
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

// Navigable preference row with a trailing chevron icon

private fun LazyListScope.navigablePreference(
    key: String,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
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

// Plain tappable preference item

private fun LazyListScope.preference(
    key: String,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
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

// Switch preference item

private fun LazyListScope.switchPreference(
    key: String,
    value: Boolean,
    title: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: (Boolean) -> Boolean = { true },
    icon: @Composable ((Boolean) -> Unit)? = null,
    summary: @Composable ((Boolean) -> Unit)? = null,
    onValueChange: (Boolean) -> Unit,
) {
    item(key = key, contentType = "SwitchPreference") {
        SwitchPreference(
            value = value,
            title = { title(value) },
            modifier = modifier,
            enabled = enabled(value),
            icon = icon?.let { { it(value) } },
            summary = summary?.let { { it(value) } },
            onValueChange = onValueChange,
        )
    }
}

private fun Modifier.preferenceCard(
    color: Color,
    shape: Shape,
): Modifier = padding(horizontal = 8.dp).background(color = color, shape = shape).clip(shape)

private fun snap(
    value: Float,
    interval: Int,
): Int {
    val raw = value.roundToInt()
    if (interval <= 1) return raw
    return ((raw + interval / 2) / interval) * interval
}
