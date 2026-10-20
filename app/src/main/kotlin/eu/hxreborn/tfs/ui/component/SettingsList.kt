package eu.hxreborn.tfs.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.ui.theme.Tokens
import eu.hxreborn.tfs.ui.util.shapeForPosition
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.PreferenceCategory

enum class RowTrailing { None, Chevron, External }

val rowSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

fun Modifier.groupedRow(
    surface: Color,
    index: Int,
    count: Int,
): Modifier =
    (if (index > 0) padding(top = Tokens.PreferenceItemGap) else this)
        .groupedPreference(surface, shapeForPosition(count, index))

@Composable
fun SectionHeader(text: String) {
    PreferenceCategory(title = { Text(text) })
}

@Composable
fun SettingsRow(
    title: String,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: ImageVector? = null,
    trailing: RowTrailing = RowTrailing.None,
) {
    Preference(
        title = { PreferenceTitle(title) },
        modifier = modifier.groupedRow(rowSurface, index, count),
        icon = icon?.let { { Icon(it, contentDescription = null) } },
        summary = summary?.let { { Text(it) } },
        widgetContainer =
            when (trailing) {
                RowTrailing.None -> {
                    null
                }

                RowTrailing.Chevron -> {
                    { TrailingIcon(Icons.AutoMirrored.Filled.KeyboardArrowRight) }
                }

                RowTrailing.External -> {
                    { TrailingIcon(Icons.AutoMirrored.Outlined.OpenInNew) }
                }
            },
        onClick = onClick,
    )
}

@Composable
private fun TrailingIcon(icon: ImageVector) {
    Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(end = Tokens.SpacingLg),
    )
}

@Composable
fun InfoRow(
    title: String,
    summary: String,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Preference(
        title = { PreferenceTitle(title) },
        modifier = modifier.groupedRow(rowSurface, index, count),
        icon = icon?.let { { Icon(it, contentDescription = null) } },
        summary = { Text(summary) },
    )
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
