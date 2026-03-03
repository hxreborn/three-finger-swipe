package eu.hxreborn.tfs.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.screen.AboutScreen
import eu.hxreborn.tfs.ui.screen.ActionPickerScreen
import eu.hxreborn.tfs.ui.screen.CaptureModeScreen
import eu.hxreborn.tfs.ui.screen.EdgeExclusionScreen
import eu.hxreborn.tfs.ui.screen.HomeScreen
import eu.hxreborn.tfs.ui.screen.LicensesScreen
import eu.hxreborn.tfs.ui.screen.TriggerDistanceScreen
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavHost(viewModel: SettingsViewModel) {
    val backStack = rememberNavBackStack(Destination.Home)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingReboot by viewModel.pendingReboot.collectAsStateWithLifecycle()
    val xposedActive by viewModel.xposedActive.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    val density = LocalDensity.current
    val slideDistance = with(density) { 30.dp.roundToPx() }
    val navigateUp = {
        if (backStack.size > 1) {
            backStack.removeLast()
        }
        Unit
    }
    val handleSystemBack = {
        if (backStack.size > 1) {
            backStack.removeLast()
        } else {
            activity?.finish()
        }
        Unit
    }

    NavDisplay(
        backStack = backStack,
        onBack = handleSystemBack,
        transitionSpec = {
            (slideInHorizontally { slideDistance } + fadeIn()) togetherWith
                (slideOutHorizontally { -slideDistance } + fadeOut())
        },
        popTransitionSpec = {
            (slideInHorizontally { -slideDistance } + fadeIn()) togetherWith
                (slideOutHorizontally { slideDistance } + fadeOut())
        },
        predictivePopTransitionSpec = {
            (slideInHorizontally { -slideDistance } + fadeIn()) togetherWith
                (slideOutHorizontally { slideDistance } + fadeOut())
        },
        entryProvider =
            entryProvider {
                entry<Destination.Home> {
                    HomeScreen(
                        state = state,
                        pendingReboot = pendingReboot,
                        onSwipeEnabledChange = { viewModel.savePref(Prefs.SWIPE_ENABLED, it) },
                        onFingerLandingChange = {
                            viewModel.savePref(Prefs.FINGER_LANDING_MS, it)
                        },
                        onCooldownChange = { viewModel.savePref(Prefs.COOLDOWN_MS, it) },
                        onDebugLogsChange = { viewModel.savePref(Prefs.DEBUG_LOGS, it) },
                        onResetToDefaults = viewModel::resetToDefaults,
                        onRestoreState = viewModel::restoreState,
                        onNavigate = { backStack.add(it) },
                    )
                }

                entry<Destination.EdgeExclusion> {
                    EdgeExclusionScreen(
                        edgeExclusionDp = state.edgeExclusionDp,
                        onValueChange = { viewModel.savePref(Prefs.EDGE_EXCLUSION_DP, it) },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.TriggerDistance> {
                    TriggerDistanceScreen(
                        swipeThresholdPct = state.swipeThresholdPct,
                        onValueChange = {
                            viewModel.savePref(Prefs.SWIPE_THRESHOLD_PCT, it)
                        },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.ActionPicker> {
                    ActionPickerScreen(
                        selectedAction = state.selectedAction,
                        onActionChange = { viewModel.savePref(Prefs.SELECTED_ACTION, it.key) },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.CaptureMode> {
                    CaptureModeScreen(
                        captureMode = state.captureMode,
                        onCaptureModeChange = {
                            viewModel.savePref(Prefs.CAPTURE_MODE, it.key)
                        },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.About> {
                    AboutScreen(
                        xposedActive = xposedActive,
                        onNavigateToLicenses = {
                            backStack.add(Destination.Licenses)
                        },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.Licenses> {
                    LicensesScreen(onBack = navigateUp)
                }
            },
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
