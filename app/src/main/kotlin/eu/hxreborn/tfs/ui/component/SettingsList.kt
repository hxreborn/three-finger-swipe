package eu.hxreborn.tfs.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.R

enum class RowTrailing { None, Chevron, External }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun Modifier.rowGap(index: Int): Modifier = if (index > 0) padding(top = ListItemDefaults.SegmentedGap) else this

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun segmentedRowColors() = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
                .semantics { heading() },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsRow(
    title: String,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: ImageVector? = null,
    trailing: RowTrailing = RowTrailing.Chevron,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        colors = segmentedRowColors(),
        modifier = modifier.rowGap(index),
        leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
        supportingContent = summary?.let { { Text(it) } },
        trailingContent =
            when (trailing) {
                RowTrailing.None -> {
                    null
                }

                RowTrailing.Chevron -> {
                    { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) }
                }

                RowTrailing.External -> {
                    { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) }
                }
            },
    ) {
        Text(title)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InfoRow(
    title: String,
    summary: String,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index, count),
        colors = segmentedRowColors(),
        modifier = modifier.rowGap(index),
        leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
        supportingContent = { Text(summary) },
    ) {
        Text(title)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChoiceRow(
    title: String,
    description: String,
    selected: Boolean,
    index: Int,
    count: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    SegmentedListItem(
        selected = selected,
        onClick = {
            if (!selected) {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onSelect()
            }
        },
        shapes = ListItemDefaults.segmentedShapes(index, count),
        colors = segmentedRowColors(),
        modifier = modifier.rowGap(index).semantics { role = Role.RadioButton },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        supportingContent = { Text(description) },
    ) {
        Text(title)
    }
}

@Composable
fun ResetAction(
    enabled: Boolean,
    onReset: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    IconButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            onReset()
        },
        enabled = enabled,
    ) {
        Icon(Icons.Outlined.RestartAlt, contentDescription = stringResource(R.string.action_reset))
    }
}
