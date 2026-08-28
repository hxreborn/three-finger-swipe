package eu.hxreborn.tfs.ui.screen

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.prefs.AppFilterMode
import eu.hxreborn.tfs.ui.util.drawVerticalScrollbar
import eu.hxreborn.tfs.ui.util.rememberAppIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val IconSize = 40.dp

private const val SYSTEM_FLAGS = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP

@Immutable
private data class LaunchableApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
)

private suspend fun loadLaunchableApps(context: Context): List<LaunchableApp> =
    withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        runCatching { pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL) }
            .getOrDefault(emptyList())
            .asSequence()
            .mapNotNull { it.activityInfo?.applicationInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { info ->
                LaunchableApp(
                    packageName = info.packageName,
                    label = runCatching { info.loadLabel(pm).toString() }.getOrDefault(info.packageName),
                    isSystem = info.flags and SYSTEM_FLAGS != 0,
                )
            }.sortedBy { it.label.lowercase() }
            .toList()
    }

@Stable
private class LaunchableAppsState(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    var apps by mutableStateOf<List<LaunchableApp>?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var userRefreshCount by mutableIntStateOf(0)
        private set

    private var job: Job? = null

    fun refresh(userInitiated: Boolean = false) {
        if (userInitiated) {
            isRefreshing = true
            userRefreshCount++
        }
        if (job?.isActive == true) return
        job =
            scope.launch {
                try {
                    apps = loadLaunchableApps(context)
                } finally {
                    isRefreshing = false
                }
            }
    }
}

@Composable
private fun rememberLaunchableApps(): LaunchableAppsState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apps = remember(context) { LaunchableAppsState(context, scope) }
    LifecycleResumeEffect(apps) {
        apps.refresh()
        onPauseOrDispose {}
    }
    return apps
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFilterScreen(
    mode: AppFilterMode,
    filteredApps: Set<String>,
    modifier: Modifier = Modifier,
    onModeChange: (AppFilterMode) -> Unit,
    onFilteredAppsChange: (Set<String>) -> Unit,
    onBack: () -> Unit,
) {
    val apps = rememberLaunchableApps()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var menuOpen by remember { mutableStateOf(false) }
    var showSystemApps by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.screen_app_filter)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_navigate_up),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.action_more_options),
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.app_filter_show_system)) },
                            onClick = {
                                menuOpen = false
                                showSystemApps = !showSystemApps
                            },
                            trailingIcon = { Checkbox(checked = showSystemApps, onCheckedChange = null) },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.app_filter_refresh)) },
                            onClick = {
                                menuOpen = false
                                apps.refresh(userInitiated = true)
                            },
                            leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.app_filter_deselect_all)) },
                            onClick = {
                                menuOpen = false
                                onFilteredAppsChange(emptySet())
                            },
                            leadingIcon = { Icon(Icons.Outlined.Deselect, contentDescription = null) },
                            enabled = filteredApps.isNotEmpty(),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        AppFilterContent(
            innerPadding = innerPadding,
            scrollBehavior = scrollBehavior,
            apps = apps,
            showSystemApps = showSystemApps,
            onShowSystemApps = { showSystemApps = it },
            mode = mode,
            selected = filteredApps,
            onModeChange = onModeChange,
            onToggle = { pkg, checked ->
                onFilteredAppsChange(if (checked) filteredApps + pkg else filteredApps - pkg)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppFilterContent(
    innerPadding: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,
    apps: LaunchableAppsState,
    showSystemApps: Boolean,
    onShowSystemApps: (Boolean) -> Unit,
    mode: AppFilterMode,
    selected: Set<String>,
    onModeChange: (AppFilterMode) -> Unit,
    onToggle: (String, Boolean) -> Unit,
) {
    val textFieldState = rememberTextFieldState()
    val query = textFieldState.text.toString()

    val pinnedSelection = remember(apps.userRefreshCount) { selected }
    val loadedApps = apps.apps
    val visibleApps =
        remember(loadedApps, query, showSystemApps, pinnedSelection) {
            loadedApps
                ?.filter { app ->
                    (showSystemApps || !app.isSystem) &&
                        (
                            query.isBlank() ||
                                app.label.contains(query, ignoreCase = true) ||
                                app.packageName.contains(query, ignoreCase = true)
                        )
                }?.sortedWith(
                    compareByDescending<LaunchableApp> { it.packageName in pinnedSelection }
                        .thenBy { it.label.lowercase() },
                )
        }
    val filtersActive = query.isNotBlank() || !showSystemApps
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()

    Column(Modifier.fillMaxSize().padding(innerPadding)) {
        ModeSelector(
            mode = mode,
            onModeChange = onModeChange,
            selectedCount = selected.size,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        TextField(
            state = textFieldState,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            lineLimits = TextFieldLineLimits.SingleLine,
            placeholder = { Text(stringResource(R.string.app_filter_search_hint)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { textFieldState.clearText() }) {
                        Icon(
                            Icons.Outlined.Clear,
                            contentDescription = stringResource(R.string.app_filter_clear_search),
                        )
                    }
                }
            },
            shape = SearchBarDefaults.inputFieldShape,
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
        )

        PullToRefreshBox(
            isRefreshing = apps.isRefreshing,
            onRefresh = { apps.refresh(userInitiated = true) },
            modifier = Modifier.weight(1f),
            state = pullState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullState,
                    isRefreshing = apps.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            },
        ) {
            if (visibleApps == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
                return@PullToRefreshBox
            }
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .drawVerticalScrollbar(listState),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (visibleApps.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            filtersActive = filtersActive,
                            onShowAll = {
                                textFieldState.clearText()
                                onShowSystemApps(true)
                            },
                            modifier = Modifier.fillParentMaxWidth(),
                        )
                    }
                } else {
                    items(visibleApps, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            checked = app.packageName in selected,
                            onCheckedChange = { onToggle(app.packageName, it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    mode: AppFilterMode,
    selectedCount: Int,
    modifier: Modifier = Modifier,
    onModeChange: (AppFilterMode) -> Unit,
) {
    Column(modifier = modifier) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            AppFilterMode.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == mode,
                    onClick = { onModeChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, AppFilterMode.entries.size),
                ) {
                    Text(
                        stringResource(option.labelRes()),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        AnimatedContent(
            targetState = mode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(top = 8.dp),
            label = "filter-mode-summary",
        ) { target ->
            Text(
                text =
                    if (selectedCount == 0) {
                        stringResource(R.string.app_filter_none)
                    } else {
                        pluralStringResource(target.summaryRes(), selectedCount, selectedCount)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(
    filtersActive: Boolean,
    modifier: Modifier = Modifier,
    onShowAll: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(if (filtersActive) R.string.app_filter_empty else R.string.app_filter_empty_none),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (filtersActive) {
            TextButton(onClick = onShowAll) {
                Text(stringResource(R.string.app_filter_show_all))
            }
        }
    }
}

@Composable
private fun AppRow(
    app: LaunchableApp,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val sizePx = with(LocalDensity.current) { IconSize.roundToPx() }
    val icon = rememberAppIcon(app.packageName, sizePx)

    ListItem(
        modifier =
            Modifier.toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        leadingContent = {
            if (icon != null) {
                Image(icon, contentDescription = null, modifier = Modifier.size(IconSize))
            } else {
                Icon(
                    Icons.Outlined.Android,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        supportingContent = {
            Text(app.packageName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            Checkbox(checked = checked, onCheckedChange = null)
        },
    ) {
        Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@StringRes
internal fun AppFilterMode.labelRes(): Int =
    when (this) {
        AppFilterMode.BLOCK -> R.string.app_filter_mode_block
        AppFilterMode.ALLOW -> R.string.app_filter_mode_allow
    }

@PluralsRes
internal fun AppFilterMode.summaryRes(): Int =
    when (this) {
        AppFilterMode.BLOCK -> R.plurals.app_filter_summary_block
        AppFilterMode.ALLOW -> R.plurals.app_filter_summary_allow
    }
