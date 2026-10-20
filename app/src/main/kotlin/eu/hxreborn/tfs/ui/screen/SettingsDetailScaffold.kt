package eu.hxreborn.tfs.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.ui.theme.Tokens

private val ScreenGutter = 16.dp
private val MaxContentWidth = 640.dp

internal val GroupedListGutter = Tokens.ScreenHorizontalPadding

internal val wideScreenInset: Dp
    @Composable get() = ((LocalConfiguration.current.screenWidthDp.dp - MaxContentWidth) / 2).coerceAtLeast(0.dp)

internal val TopBarInsets: WindowInsets
    @Composable get() = TopAppBarDefaults.windowInsets.add(WindowInsets(left = wideScreenInset, right = wideScreenInset))

internal val TopLevelInsets: WindowInsets
    @Composable get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

@Composable
internal fun PaddingValues.withScreenGutter(
    bottom: Dp = ScreenGutter,
    horizontal: Dp = ScreenGutter,
): PaddingValues {
    val direction = LocalLayoutDirection.current
    val inset = wideScreenInset
    return PaddingValues(
        start = calculateStartPadding(direction) + horizontal + inset,
        end = calculateEndPadding(direction) + horizontal + inset,
        top = calculateTopPadding(),
        bottom = calculateBottomPadding() + bottom,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
) {
    MediumFlexibleTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_navigate_up),
                )
            }
        },
        actions = actions,
        windowInsets = TopBarInsets,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsDetailScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    gutter: Dp = ScreenGutter,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { DetailTopBar(title, onBack, scrollBehavior, actions) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding.withScreenGutter(horizontal = gutter))
                    .padding(top = 8.dp),
            content = content,
        )
    }
}
