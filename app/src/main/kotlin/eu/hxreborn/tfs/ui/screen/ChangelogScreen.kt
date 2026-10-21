package eu.hxreborn.tfs.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Upgrade
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.ui.util.ReleaseNote
import eu.hxreborn.tfs.ui.util.rememberReleaseNotes
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val ChipRadius = 8.dp
private val BadgeHeight = 22.dp
private val BadgeIconSize = 14.dp
private val NoteMinHeight = 40.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChangelogScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notes = rememberReleaseNotes()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { DetailTopBar(stringResource(R.string.about_changelog), onBack, scrollBehavior) },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimatedVisibility(
                    visible = listState.canScrollBackward,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    SmallFloatingActionButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                        Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = stringResource(R.string.changelog_jump_top))
                    }
                }
                AnimatedVisibility(
                    visible = listState.canScrollForward,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch { listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1) }
                        },
                    ) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = stringResource(R.string.changelog_jump_bottom))
                    }
                }
            }
        },
    ) { innerPadding ->
        if (notes == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding.withScreenGutter(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(notes, key = { it.versionCode }) { note ->
                ReleaseCard(note)
            }
        }
    }
}

@Composable
internal fun ReleaseCard(
    note: ReleaseNote,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.padding(horizontal = 4.dp).semantics { heading() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.changelog_version, note.versionName), style = MaterialTheme.typography.titleMedium)
                Text(
                    note.releasedOn?.let(::formatReleaseDate).orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                val tag =
                    when {
                        note.versionCode == BuildConfig.VERSION_CODE -> stringResource(R.string.changelog_installed)
                        note.isPreRelease -> stringResource(R.string.changelog_pre_release)
                        else -> null
                    }
                tag?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            ReleaseLines(note)
        }
    }
}

internal fun formatReleaseDate(date: LocalDate): String = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

@Composable
internal fun ReleaseLines(note: ReleaseNote) {
    Column { note.lines.forEach { NoteRow(it) } }
}

@Composable
private fun NoteRow(line: ReleaseNote.Line) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = NoteMinHeight)
            .padding(horizontal = 4.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KindBadge(line.kind)
        Text(line.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KindBadge(kind: ReleaseNote.Line.Kind) {
    val scheme = MaterialTheme.colorScheme
    val (label, icon, colors) =
        when (kind) {
            ReleaseNote.Line.Kind.NEW -> {
                Triple(R.string.notes_new, Icons.Outlined.AutoAwesome, scheme.primaryContainer to scheme.onPrimaryContainer)
            }

            ReleaseNote.Line.Kind.FIXED -> {
                Triple(R.string.notes_fixed, Icons.Outlined.Build, scheme.tertiaryContainer to scheme.onTertiaryContainer)
            }

            ReleaseNote.Line.Kind.CHANGED -> {
                Triple(R.string.notes_changed, Icons.Outlined.Tune, scheme.surfaceContainerHighest to scheme.onSurfaceVariant)
            }

            ReleaseNote.Line.Kind.UPDATED -> {
                Triple(R.string.notes_updated, Icons.Outlined.Upgrade, scheme.surfaceContainerHighest to scheme.onSurfaceVariant)
            }

            ReleaseNote.Line.Kind.REMOVED -> {
                Triple(
                    R.string.notes_removed,
                    Icons.Outlined.RemoveCircleOutline,
                    scheme.surfaceContainerHighest to scheme.onSurfaceVariant,
                )
            }

            ReleaseNote.Line.Kind.OTHER -> {
                Triple(R.string.notes_other, Icons.Outlined.Code, scheme.surfaceContainerHighest to scheme.onSurfaceVariant)
            }
        }
    Surface(shape = RoundedCornerShape(ChipRadius), color = colors.first, contentColor = colors.second) {
        Row(
            Modifier.heightIn(min = BadgeHeight).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(BadgeIconSize))
            Text(
                stringResource(label),
                style = MaterialTheme.typography.labelSmallEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
