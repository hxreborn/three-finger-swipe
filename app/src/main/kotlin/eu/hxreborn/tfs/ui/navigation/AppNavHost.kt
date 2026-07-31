package eu.hxreborn.tfs.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.screen.AboutScreen
import eu.hxreborn.tfs.ui.screen.CaptureModeScreen
import eu.hxreborn.tfs.ui.screen.EdgeExclusionScreen
import eu.hxreborn.tfs.ui.screen.HomeScreen
import eu.hxreborn.tfs.ui.screen.LicensesScreen
import eu.hxreborn.tfs.ui.screen.SplitMethodScreen
import eu.hxreborn.tfs.ui.screen.TriggerDistanceScreen
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavHost(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(Destination.Home)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val xposedServiceAvailable by viewModel.xposedServiceAvailable.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    val density = LocalDensity.current
    val slideDistance = with(density) { 30.dp.roundToPx() }
    val currentState = rememberUpdatedState(state)
    val currentXposedServiceAvailable = rememberUpdatedState(xposedServiceAvailable)
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
    val enterSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val exitSpatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<IntOffset>()
    val enterEffectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val exitEffectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val screenEntryProvider =
        remember(backStack, navigateUp, viewModel) {
            entryProvider<NavKey> {
                entry<Destination.Home> {
                    HomeScreen(
                        state = currentState.value,
                        onActionChange = { viewModel.savePref(Prefs.SELECTED_ACTION, it.key) },
                        onFingerLandingChange = {
                            viewModel.savePref(Prefs.FINGER_LANDING_MS, it)
                        },
                        onCooldownChange = { viewModel.savePref(Prefs.COOLDOWN_MS, it) },
                        onResetToDefaults = viewModel::resetToDefaults,
                        onRestoreState = viewModel::restoreState,
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

                entry<Destination.CaptureMode> {
                    CaptureModeScreen(
                        captureMode = currentState.value.captureMode,
                        onCaptureModeChange = {
                            viewModel.savePref(Prefs.CAPTURE_MODE, it.key)
                        },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.SplitMethod> {
                    SplitMethodScreen(
                        splitMethod = currentState.value.splitMethod,
                        onSplitMethodChange = {
                            viewModel.savePref(Prefs.SPLIT_METHOD, it.key)
                        },
                        onBack = navigateUp,
                    )
                }

                entry<Destination.About> {
                    AboutScreen(
                        xposedServiceAvailable = currentXposedServiceAvailable.value,
                        onNavigateToLicenses = {
                            backStack.add(Destination.Licenses)
                        },
                        onTriggerHotReload = viewModel::triggerHotReload,
                        onBack = navigateUp,
                    )
                }

                entry<Destination.Licenses> {
                    LicensesScreen(onBack = navigateUp)
                }
            }
        }

    NavDisplay(
        modifier = modifier,
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
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
