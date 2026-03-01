package eu.hxreborn.tfs.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.PrefsState
import eu.hxreborn.tfs.ui.component.GestureIllustration
import eu.hxreborn.tfs.ui.theme.Tokens
import eu.hxreborn.tfs.ui.util.shapeForPosition
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModel
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SliderPreference
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preferenceCategory
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        state = state,
        onSwipeEnabledChange = { viewModel.savePref(Prefs.SWIPE_ENABLED, it) },
        onSwipeThresholdChange = { viewModel.savePref(Prefs.SWIPE_THRESHOLD_PCT, it) },
        onEdgeExclusionChange = { viewModel.savePref(Prefs.EDGE_EXCLUSION_DP, it) },
        onFingerLandingChange = { viewModel.savePref(Prefs.FINGER_LANDING_MS, it) },
        onCooldownChange = { viewModel.savePref(Prefs.COOLDOWN_MS, it) },
        onCaptureModeChange = { viewModel.savePref(Prefs.CAPTURE_MODE, it.key) },
        onDebugLogsChange = { viewModel.savePref(Prefs.DEBUG_LOGS, it) },
        onResetToDefaults = viewModel::resetToDefaults,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: PrefsState,
    onSwipeEnabledChange: (Boolean) -> Unit,
    onSwipeThresholdChange: (Int) -> Unit,
    onEdgeExclusionChange: (Int) -> Unit,
    onFingerLandingChange: (Int) -> Unit,
    onCooldownChange: (Int) -> Unit,
    onCaptureModeChange: (CaptureMode) -> Unit,
    onDebugLogsChange: (Boolean) -> Unit,
    onResetToDefaults: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    val isExpanded = LocalTextStyle.current.fontSize >= MaterialTheme.typography.headlineMedium.fontSize
                    Text(
                        text = stringResource(R.string.app_name),
                        style =
                            if (isExpanded) {
                                MaterialTheme.typography.headlineLarge.copy(
                                    lineHeight = Tokens.ExpandedTitleLineHeight,
                                )
                            } else {
                                LocalTextStyle.current
                            },
                        maxLines = if (isExpanded) Tokens.ExpandedTitleMaxLines else 1,
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        val surface = MaterialTheme.colorScheme.surfaceVariant

        ProvidePreferenceLocals {
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

                // ── Screenshot gesture ───────────────────────────────────────────

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
                    title = {
                        Text(
                            text = stringResource(R.string.pref_swipe_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = { Text(stringResource(R.string.pref_swipe_summary)) },
                    onValueChange = onSwipeEnabledChange,
                )

                // ── Gesture tuning ───────────────────────────────────────────────

                preferenceCategory(
                    key = "category_gesture_tuning",
                    title = { Text(stringResource(R.string.category_gesture_tuning)) },
                )

                intSliderPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(4, 0)),
                    key = Prefs.SWIPE_THRESHOLD_PCT.key,
                    value = state.swipeThresholdPct,
                    valueRange = Prefs.SWIPE_THRESHOLD_PCT.sliderRange!!,
                    icon = { Icon(Icons.Outlined.SwipeDown, contentDescription = null) },
                    title = {
                        Text(
                            text = stringResource(R.string.pref_swipe_threshold_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = { Text(stringResource(R.string.pref_swipe_threshold_summary)) },
                    valueText = { Text("$it%") },
                    onValueChange = onSwipeThresholdChange,
                )

                preferenceSpacer("spacer_threshold")

                intSliderPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(4, 1)),
                    key = Prefs.EDGE_EXCLUSION_DP.key,
                    value = state.edgeExclusionDp,
                    valueRange = Prefs.EDGE_EXCLUSION_DP.sliderRange!!,
                    icon = { Icon(Icons.Outlined.BorderOuter, contentDescription = null) },
                    title = {
                        Text(
                            text = stringResource(R.string.pref_edge_exclusion_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = { Text(stringResource(R.string.pref_edge_exclusion_summary)) },
                    valueText = { Text("$it dp") },
                    onValueChange = onEdgeExclusionChange,
                )

                preferenceSpacer("spacer_exclusion")

                intSliderPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(4, 2)),
                    key = Prefs.FINGER_LANDING_MS.key,
                    value = state.fingerLandingMs,
                    valueRange = Prefs.FINGER_LANDING_MS.sliderRange!!,
                    icon = { Icon(Icons.Outlined.HourglassEmpty, contentDescription = null) },
                    title = {
                        Text(
                            text = stringResource(R.string.pref_finger_landing_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = { Text(stringResource(R.string.pref_finger_landing_summary)) },
                    valueText = { Text("$it ms") },
                    onValueChange = onFingerLandingChange,
                )

                preferenceSpacer("spacer_landing")

                intSliderPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(4, 3)),
                    key = Prefs.COOLDOWN_MS.key,
                    value = state.cooldownMs,
                    valueRange = Prefs.COOLDOWN_MS.sliderRange!!,
                    icon = { Icon(Icons.Outlined.Timer, contentDescription = null) },
                    title = {
                        Text(
                            text = stringResource(R.string.pref_cooldown_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = { Text(stringResource(R.string.pref_cooldown_summary)) },
                    valueText = { Text("$it ms") },
                    onValueChange = onCooldownChange,
                )

                // ── Advanced ─────────────────────────────────────────────────────

                preferenceCategory(
                    key = "category_advanced",
                    title = { Text(stringResource(R.string.category_advanced)) },
                )

                item(key = Prefs.CAPTURE_MODE.key, contentType = "ListPreference") {
                    val labelReflection = stringResource(R.string.pref_capture_mode_reflection)
                    val labelSysrq = stringResource(R.string.pref_capture_mode_sysrq)
                    val descReflection = stringResource(R.string.pref_capture_mode_reflection_desc)
                    val descSysrq = stringResource(R.string.pref_capture_mode_sysrq_desc)
                    ListPreference(
                        value = state.captureMode,
                        onValueChange = onCaptureModeChange,
                        values = CaptureMode.entries,
                        modifier = Modifier.preferenceCard(surface, shapeForPosition(3, 0)),
                        icon = { Icon(Icons.Outlined.CameraAlt, contentDescription = null) },
                        title = {
                            Text(
                                text = stringResource(R.string.pref_capture_mode_title),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        summary = {
                            Text(
                                text =
                                    when (state.captureMode) {
                                        CaptureMode.SYSRQ -> labelSysrq
                                        CaptureMode.REFLECTION -> labelReflection
                                    },
                            )
                        },
                        valueToText = { mode ->
                            AnnotatedString(
                                when (mode) {
                                    CaptureMode.SYSRQ -> descSysrq
                                    CaptureMode.REFLECTION -> descReflection
                                },
                            )
                        },
                    )
                }

                preferenceSpacer("spacer_capture")

                switchPreference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(3, 1)),
                    key = Prefs.DEBUG_LOGS.key,
                    value = state.debugLogs,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.BugReport,
                            contentDescription = null,
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.pref_debug_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = { Text(stringResource(R.string.pref_debug_summary)) },
                    onValueChange = onDebugLogsChange,
                )

                preferenceSpacer("spacer_debug")

                preference(
                    modifier = Modifier.preferenceCard(surface, shapeForPosition(3, 2)),
                    key = "reset_to_defaults",
                    icon = { Icon(Icons.Outlined.RestartAlt, contentDescription = null) },
                    title = {
                        Text(
                            text = stringResource(R.string.pref_reset_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    summary = { Text(stringResource(R.string.pref_reset_summary)) },
                    onClick = onResetToDefaults,
                )
            }
        }
    }
}

// ── Spacer between grouped preference items ──────────────────────────────────

private fun LazyListScope.preferenceSpacer(key: String) {
    item(key = key, contentType = "spacer") { Spacer(Modifier.height(2.dp)) }
}

// ── Int slider backed by ViewModel state ─────────────────────────────────────
// Bridges the Int ViewModel state to the Float-based SliderPreference composable.
// sliderValue tracks in-progress drag locally; onValueChange fires on finger up.

private inline fun LazyListScope.intSliderPreference(
    key: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    noinline title: @Composable () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    noinline icon: @Composable (() -> Unit)? = null,
    noinline summary: @Composable (() -> Unit)? = null,
    noinline valueText: @Composable (Int) -> Unit,
    noinline onValueChange: (Int) -> Unit,
) {
    item(key = key, contentType = "SliderPreference") {
        // reset slider position whenever the underlying pref changes from outside
        var sliderValue by remember(value) { mutableFloatStateOf(value.toFloat()) }
        SliderPreference(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            sliderValue = sliderValue,
            onSliderValueChange = { sliderValue = it },
            title = title,
            modifier = modifier,
            valueRange = valueRange,
            enabled = true,
            icon = icon,
            summary = summary,
            valueText = { valueText(sliderValue.roundToInt()) },
        )
    }
}

// ── Plain tappable preference item ───────────────────────────────────────────

private inline fun LazyListScope.preference(
    key: String,
    noinline title: @Composable () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    noinline icon: @Composable (() -> Unit)? = null,
    noinline summary: @Composable (() -> Unit)? = null,
    noinline onClick: () -> Unit,
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

// ── Switch preference item ────────────────────────────────────────────────────

private inline fun LazyListScope.switchPreference(
    key: String,
    value: Boolean,
    crossinline title: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    crossinline enabled: (Boolean) -> Boolean = { true },
    noinline icon: @Composable ((Boolean) -> Unit)? = null,
    noinline summary: @Composable ((Boolean) -> Unit)? = null,
    noinline onValueChange: (Boolean) -> Unit,
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
