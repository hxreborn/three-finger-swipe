package eu.hxreborn.tfs.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.component.sectionPreferenceTheme
import eu.hxreborn.tfs.ui.screen.AboutScreen
import eu.hxreborn.tfs.ui.screen.AppFilterScreen
import eu.hxreborn.tfs.ui.screen.EdgeExclusionScreen
import eu.hxreborn.tfs.ui.screen.HomeScreen
import eu.hxreborn.tfs.ui.screen.LicensesScreen
import eu.hxreborn.tfs.ui.screen.SettingsScreen
import eu.hxreborn.tfs.ui.screen.TriggerDistanceScreen
import eu.hxreborn.tfs.ui.viewmodel.ModuleStatus
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModel
import me.zhanghai.compose.preference.ProvidePreferenceLocals

private val NavBarHeight = 64.dp
private val NavBarWidth = 248.dp
private val NavBarMargin = 16.dp

private class TopLevelTab(
    val destination: Destination,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
)

private val topLevelTabs =
    listOf(
        TopLevelTab(Destination.Home, R.string.screen_home, Icons.Filled.Home, Icons.Outlined.Home),
        TopLevelTab(Destination.Settings, R.string.screen_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
    )

@Composable
fun AppNavHost(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(Destination.Home)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val moduleStatus by viewModel.moduleStatus.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val slideDistance = with(density) { 30.dp.roundToPx() }
    val currentState = rememberUpdatedState(state)
    val currentModuleStatus = rememberUpdatedState(moduleStatus)
    var navBarHeight by remember { mutableStateOf(NavBarHeight) }
    val topLevelBottomPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + navBarHeight + NavBarMargin * 2
    val currentBottomPadding = rememberUpdatedState(topLevelBottomPadding)
    val currentTab = backStack.lastOrNull()?.let { top -> topLevelTabs.firstOrNull { it.destination == top } }

    LifecycleResumeEffect(viewModel) {
        viewModel.refreshModuleStatus()
        onPauseOrDispose {}
    }

    val navigateUp =
        remember(backStack) {
            {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                }
                Unit
            }
        }
    val handleSystemBack =
        remember(backStack, activity) {
            {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                } else {
                    activity?.finish()
                }
                Unit
            }
        }
    val selectTab =
        remember(backStack) {
            { tab: Destination ->
                while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                if (tab != Destination.Home) backStack.add(tab)
            }
        }

    val enterSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val exitSpatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<IntOffset>()
    val enterEffectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val exitEffectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val tabFade: AnimatedContentTransitionScope<Scene<*>>.() -> ContentTransform = {
        fadeIn(animationSpec = enterEffectsSpec) togetherWith fadeOut(animationSpec = exitEffectsSpec)
    }
    val screenEntryProvider =
        remember(backStack, navigateUp, viewModel) {
            entryProvider<NavKey> {
                entry<Destination.Home> {
                    HomeScreen(
                        state = currentState.value,
                        status = currentModuleStatus.value,
                        bottomPadding = currentBottomPadding.value,
                        onActionChange = { viewModel.savePref(Prefs.SELECTED_ACTION, it.key) },
                        onFingerLandingChange = {
                            viewModel.savePref(Prefs.FINGER_LANDING_MS, it)
                        },
                        onCooldownChange = { viewModel.savePref(Prefs.COOLDOWN_MS, it) },
                        onNavigate = { backStack.add(it) },
                    )
                }

                entry<Destination.Settings>(
                    metadata =
                        NavDisplay.transitionSpec(tabFade) +
                            NavDisplay.popTransitionSpec(tabFade) +
                            NavDisplay.predictivePopTransitionSpec { _ -> tabFade() },
                ) {
                    SettingsScreen(
                        state = currentState.value,
                        bottomPadding = currentBottomPadding.value,
                        onResetToDefaults = viewModel::resetToDefaults,
                        onRestoreState = viewModel::restoreState,
                        onCaptureModeChange = { viewModel.savePref(Prefs.CAPTURE_MODE, it.key) },
                        onSplitMethodChange = { viewModel.savePref(Prefs.SPLIT_METHOD, it.key) },
                        onNavigate = { backStack.add(it) },
                    )
                }

                entry<Destination.EdgeExclusion> {
                    EdgeExclusionScreen(
                        edgeExclusionDp = currentState.value.edgeExclusionDp,
                        onValueChange = {
                            viewModel.savePref(Prefs.EDGE_EXCLUSION_DP, it)
                        },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.TriggerDistance> {
                    TriggerDistanceScreen(
                        swipeThresholdPct = currentState.value.swipeThresholdPct,
                        onValueChange = {
                            viewModel.savePref(Prefs.SWIPE_THRESHOLD_PCT, it)
                        },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.AppFilter> {
                    AppFilterScreen(
                        mode = currentState.value.appFilterMode,
                        filteredApps = currentState.value.filteredApps,
                        onModeChange = { viewModel.savePref(Prefs.APP_FILTER_MODE, it.key) },
                        onFilteredAppsChange = { viewModel.savePref(Prefs.FILTERED_APPS, it) },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.About> {
                    AboutScreen(
                        xposedServiceAvailable = currentModuleStatus.value !is ModuleStatus.Inactive,
                        onNavigateToLicenses = { backStack.add(Destination.Licenses) },
                        onTriggerHotReload = viewModel::triggerHotReload,
                        onBack = navigateUp,
                    )
                }

                entry<Destination.Licenses> {
                    LicensesScreen(onBack = navigateUp)
                }
            }
        }

    ProvidePreferenceLocals(theme = sectionPreferenceTheme()) {
        Box(modifier = modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = handleSystemBack,
                transitionSpec = {
                    (
                        slideInHorizontally(
                            animationSpec = enterSpatialSpec,
                        ) { slideDistance } + fadeIn(animationSpec = enterEffectsSpec)
                    ) togetherWith
                        (
                            slideOutHorizontally(
                                animationSpec = exitSpatialSpec,
                            ) { -slideDistance } + fadeOut(animationSpec = exitEffectsSpec)
                        )
                },
                popTransitionSpec = {
                    (
                        slideInHorizontally(
                            animationSpec = enterSpatialSpec,
                        ) { -slideDistance } + fadeIn(animationSpec = enterEffectsSpec)
                    ) togetherWith
                        (
                            slideOutHorizontally(
                                animationSpec = exitSpatialSpec,
                            ) { slideDistance } + fadeOut(animationSpec = exitEffectsSpec)
                        )
                },
                predictivePopTransitionSpec = {
                    (
                        slideInHorizontally(
                            animationSpec = enterSpatialSpec,
                        ) { -slideDistance } + fadeIn(animationSpec = enterEffectsSpec)
                    ) togetherWith
                        (
                            slideOutHorizontally(
                                animationSpec = exitSpatialSpec,
                            ) { slideDistance } + fadeOut(animationSpec = exitEffectsSpec)
                        )
                },
                entryProvider = screenEntryProvider,
            )

            AnimatedVisibility(
                visible = currentTab != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(animationSpec = enterSpatialSpec) { it } + fadeIn(animationSpec = enterEffectsSpec),
                exit = slideOutVertically(animationSpec = exitSpatialSpec) { it } + fadeOut(animationSpec = exitEffectsSpec),
            ) {
                FloatingNavBar(
                    modifier = Modifier.onSizeChanged { navBarHeight = with(density) { it.height.toDp() } },
                    selected = currentTab?.destination,
                    onSelect = { tab ->
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        selectTab(tab)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FloatingNavBar(
    selected: Destination?,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = NavBarMargin)
                .width(NavBarWidth)
                .then(modifier),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.heightIn(min = NavBarHeight).padding(horizontal = 8.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            topLevelTabs.forEach { tab ->
                val isSelected = tab.destination == selected
                ShortNavigationBarItem(
                    selected = isSelected,
                    onClick = { if (!isSelected) onSelect(tab.destination) },
                    icon = { Icon(if (isSelected) tab.selectedIcon else tab.icon, contentDescription = null) },
                    label = { Text(stringResource(tab.labelRes)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
