package eu.hxreborn.tfs.ui.screen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AppBlocking
import androidx.compose.material.icons.outlined.BorderOuter
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.prefs.AppPrefs
import eu.hxreborn.tfs.prefs.IntPref
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.component.GestureIllustration
import eu.hxreborn.tfs.ui.component.MorphingLogo
import eu.hxreborn.tfs.ui.component.PreferenceTitle
import eu.hxreborn.tfs.ui.component.RowTrailing
import eu.hxreborn.tfs.ui.component.SectionHeader
import eu.hxreborn.tfs.ui.component.SettingsRow
import eu.hxreborn.tfs.ui.component.groupedRow
import eu.hxreborn.tfs.ui.component.rowSurface
import eu.hxreborn.tfs.ui.navigation.Destination
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.theme.Tokens
import eu.hxreborn.tfs.ui.viewmodel.Framework
import eu.hxreborn.tfs.ui.viewmodel.ModuleStatus
import me.zhanghai.compose.preference.SliderPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: AppPrefs,
    status: ModuleStatus,
    bottomPadding: Dp,
    onActionChange: (ActionId) -> Unit,
    onFingerLandingChange: (Int) -> Unit,
    onCooldownChange: (Int) -> Unit,
    onNavigate: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showActionDialog by rememberSaveable { mutableStateOf(false) }

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
        contentWindowInsets = TopLevelInsets,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                windowInsets = TopBarInsets,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding.withScreenGutter(bottomPadding, horizontal = GroupedListGutter),
        ) {
            item(key = "status", contentType = "status") {
                StatusCard(status = status, modifier = Modifier.padding(horizontal = GroupedListGutter))
            }

            item(key = "illustration", contentType = "illustration") {
                GestureIllustration(modifier = Modifier.padding(top = 8.dp, start = GroupedListGutter, end = GroupedListGutter))
            }

            item(key = "header_gesture", contentType = "header") {
                SectionHeader(stringResource(R.string.category_gesture))
            }
            item(key = "action", contentType = "row") {
                SettingsRow(
                    title = stringResource(R.string.pref_swipe_down_action_title),
                    summary = stringResource(state.selectedAction.labelRes()),
                    icon = Icons.Outlined.Gesture,
                    index = 0,
                    count = 3,
                    trailing = RowTrailing.None,
                    onClick = { showActionDialog = true },
                )
            }
            item(key = "trigger_distance", contentType = "row") {
                SettingsRow(
                    title = stringResource(R.string.screen_trigger_distance),
                    summary = stringResource(R.string.value_percent, state.swipeThresholdPct),
                    icon = Icons.Outlined.SwipeDown,
                    index = 1,
                    count = 3,
                    onClick = { onNavigate(Destination.TriggerDistance) },
                )
            }
            item(key = "edge_exclusion", contentType = "row") {
                SettingsRow(
                    title = stringResource(R.string.screen_edge_exclusion),
                    summary = stringResource(R.string.value_dp, state.edgeExclusionDp),
                    icon = Icons.Outlined.BorderOuter,
                    index = 2,
                    count = 3,
                    onClick = { onNavigate(Destination.EdgeExclusion) },
                )
            }

            item(key = "header_timing", contentType = "header") {
                SectionHeader(stringResource(R.string.category_timing))
            }
            item(key = Prefs.FINGER_LANDING_MS.key, contentType = "timing") {
                TimingRow(
                    pref = Prefs.FINGER_LANDING_MS,
                    value = state.fingerLandingMs,
                    icon = Icons.Outlined.HourglassEmpty,
                    titleRes = R.string.pref_finger_landing_title,
                    summaryRes = R.string.pref_finger_landing_summary,
                    index = 0,
                    count = 2,
                    onValueChange = onFingerLandingChange,
                )
            }
            item(key = Prefs.COOLDOWN_MS.key, contentType = "timing") {
                TimingRow(
                    pref = Prefs.COOLDOWN_MS,
                    value = state.cooldownMs,
                    icon = Icons.Outlined.Timer,
                    titleRes = R.string.pref_cooldown_title,
                    summaryRes = R.string.pref_cooldown_summary,
                    index = 1,
                    count = 2,
                    onValueChange = onCooldownChange,
                )
            }

            item(key = "header_apps", contentType = "header") {
                SectionHeader(stringResource(R.string.category_apps))
            }
            item(key = "app_filter", contentType = "row") {
                SettingsRow(
                    title = stringResource(R.string.screen_app_filter),
                    summary =
                        if (state.filteredApps.isEmpty()) {
                            stringResource(R.string.pref_app_filter_summary_off)
                        } else {
                            pluralStringResource(
                                state.appFilterMode.summaryRes(),
                                state.filteredApps.size,
                                state.filteredApps.size,
                            )
                        },
                    icon = Icons.Outlined.AppBlocking,
                    index = 0,
                    count = 1,
                    onClick = { onNavigate(Destination.AppFilter) },
                )
            }
        }
    }
}

private val BadgeSize = 48.dp
private val BadgeIconSize = 28.dp

private data class StatusContent(
    @StringRes val titleRes: Int,
    val body: String,
    val healthy: Boolean,
)

@Composable
private fun ModuleStatus.content(): StatusContent =
    when (this) {
        is ModuleStatus.Running -> {
            StatusContent(R.string.status_running_title, framework.label(), healthy = true)
        }

        is ModuleStatus.Enabled -> {
            StatusContent(R.string.status_enabled_title, framework.label(), healthy = true)
        }

        is ModuleStatus.RebootNeeded -> {
            StatusContent(R.string.status_reboot_title, stringResource(R.string.status_reboot_body), healthy = false)
        }

        is ModuleStatus.Outdated -> {
            StatusContent(
                R.string.status_outdated_title,
                stringResource(R.string.status_outdated_body, loadedVersionCode, BuildConfig.VERSION_NAME),
                healthy = false,
            )
        }

        ModuleStatus.Inactive -> {
            StatusContent(R.string.status_inactive_title, stringResource(R.string.status_inactive_body), healthy = false)
        }
    }

@Composable
private fun StatusCard(
    status: ModuleStatus,
    modifier: Modifier = Modifier,
) {
    val content = status.content()
    val colors = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        if (content.healthy) colors.surfaceContainerHigh else colors.errorContainer,
        label = "statusContainer",
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Tokens.GroupedShapeCornerLarge),
        color = containerColor,
    ) {
        AnimatedContent(
            targetState = content,
            contentKey = { it.titleRes },
            transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
            label = "statusContent",
        ) { shown ->
            Row(
                modifier = Modifier.padding(Tokens.SpacingLg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Tokens.SpacingLg),
            ) {
                StatusBadge(healthy = shown.healthy)
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                ) {
                    Text(
                        text = stringResource(shown.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (shown.healthy) colors.onSurface else colors.onErrorContainer,
                    )
                    Text(
                        text = shown.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (shown.healthy) colors.onSurfaceVariant else colors.onErrorContainer,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusBadge(healthy: Boolean) {
    val colors = MaterialTheme.colorScheme
    if (healthy) {
        MorphingLogo(modifier = Modifier.size(BadgeSize)) {
            Box(Modifier.matchParentSize().background(colors.primary))
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(BadgeIconSize),
            )
        }
        return
    }
    Box(
        modifier =
            Modifier
                .size(BadgeSize)
                .background(color = colors.error, shape = MaterialShapes.Sunny.toShape()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.PriorityHigh,
            contentDescription = null,
            tint = colors.onError,
            modifier = Modifier.size(BadgeIconSize),
        )
    }
}

@Composable
internal fun Framework.label(): String = stringResource(R.string.status_framework, name, version, apiVersion)

@Composable
private fun TimingRow(
    pref: IntPref,
    value: Int,
    icon: ImageVector,
    @StringRes titleRes: Int,
    @StringRes summaryRes: Int,
    index: Int,
    count: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    var sliderValue by remember(value) { mutableFloatStateOf(value.toFloat()) }
    val range = pref.sliderRange!!
    val step = pref.step ?: 1
    SliderPreference(
        value = value.toFloat(),
        onValueChange = {
            val committed = it.roundToInt()
            if (committed != value) {
                haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
                onValueChange(committed)
            }
        },
        sliderValue = sliderValue,
        onSliderValueChange = { sliderValue = it },
        title = { PreferenceTitle(stringResource(titleRes)) },
        modifier = modifier.groupedRow(rowSurface, index, count),
        valueRange = range,
        valueSteps = ((range.endInclusive - range.start) / step).roundToInt() - 1,
        icon = { Icon(icon, contentDescription = null) },
        summary = { Text(stringResource(summaryRes)) },
        valueText = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.value_ms, sliderValue.roundToInt()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        onValueChange(pref.default)
                    },
                    enabled = value != pref.default,
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = stringResource(R.string.action_reset))
                }
            }
        },
    )
}

@Composable
private fun ActionPickerDialog(
    selectedAction: ActionId,
    onActionChange: (ActionId) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
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
                                .selectable(
                                    selected = selectedAction == action,
                                    onClick = {
                                        if (action != selectedAction) {
                                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        }
                                        onActionChange(action)
                                    },
                                    role = Role.RadioButton,
                                ).padding(vertical = 12.dp),
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

@StringRes
internal fun ActionId.labelRes(): Int =
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
            status = ModuleStatus.Running(Framework("LSPosed", "2.0", 102)),
            bottomPadding = 96.dp,
            onActionChange = {},
            onFingerLandingChange = {},
            onCooldownChange = {},
            onNavigate = {},
        )
    }
}
