package eu.hxreborn.tfs.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Gesture
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.PrefsState
import eu.hxreborn.tfs.ui.component.GestureIllustration
import eu.hxreborn.tfs.ui.theme.Tokens
import eu.hxreborn.tfs.ui.util.shapeForPosition
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModel
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preferenceCategory

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val onSwipeEnabledChange = remember<(Boolean) -> Unit> { { viewModel.setSwipeEnabled(it) } }
    val onDebugLogsChange = remember<(Boolean) -> Unit> { { viewModel.setDebugLogs(it) } }

    SettingsContent(
        state = state,
        onSwipeEnabledChange = onSwipeEnabledChange,
        onDebugLogsChange = onDebugLogsChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: PrefsState,
    onSwipeEnabledChange: (Boolean) -> Unit,
    onDebugLogsChange: (Boolean) -> Unit,
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

                preferenceCategory(
                    key = "category_gestures",
                    title = { Text(stringResource(R.string.category_screenshot_gesture)) },
                )

                val gestureCount = 1
                val swipeShape = shapeForPosition(gestureCount, 0)
                switchPreference(
                    modifier = Modifier.preferenceCard(surface, swipeShape),
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
                    summary = {
                        Text(stringResource(R.string.pref_swipe_summary))
                    },
                    onValueChange = onSwipeEnabledChange,
                )

                preferenceCategory(
                    key = "category_advanced",
                    title = { Text(stringResource(R.string.category_advanced)) },
                )

                val debugShape = shapeForPosition(1, 0)
                switchPreference(
                    modifier = Modifier.preferenceCard(surface, debugShape),
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
                    summary = {
                        Text(stringResource(R.string.pref_debug_summary))
                    },
                    onValueChange = onDebugLogsChange,
                )
            }
        }
    }
}

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
