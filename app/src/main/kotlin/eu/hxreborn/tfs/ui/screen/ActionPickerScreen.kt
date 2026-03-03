package eu.hxreborn.tfs.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.util.shapeForPosition

@Composable
fun ActionPickerScreen(
    selectedAction: ActionId,
    onActionChange: (ActionId) -> Unit,
    onBack: () -> Unit,
) {
    val defaultAction = ActionId.fromKey(Prefs.SELECTED_ACTION.default)

    SettingsDetailScaffold(
        title = stringResource(R.string.screen_action_picker),
        onBack = onBack,
        actions = {
            IconButton(
                onClick = { onActionChange(defaultAction) },
                enabled = selectedAction != defaultAction,
            ) {
                Icon(
                    Icons.Outlined.RestartAlt,
                    contentDescription = stringResource(R.string.action_reset),
                )
            }
        },
    ) {
        val actions = ActionId.entries
        Column(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
        ) {
            actions.forEachIndexed { index, action ->
                if (index > 0) {
                    Spacer(Modifier.height(2.dp))
                }
                ActionCard(
                    selected = selectedAction == action,
                    title = stringResource(action.labelRes()),
                    shape = shapeForPosition(actions.size, index),
                    onClick = { onActionChange(action) },
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    selected: Boolean,
    title: String,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@StringRes
internal fun ActionId.labelRes(): Int =
    when (this) {
        ActionId.SCREENSHOT -> R.string.action_screenshot
        ActionId.RECENT_APPS -> R.string.action_recent_apps
        ActionId.SEARCH_ASSISTANT -> R.string.action_search_assistant
        ActionId.VOICE_SEARCH -> R.string.action_voice_search
        ActionId.LAUNCH_CAMERA -> R.string.action_launch_camera
        ActionId.SCREEN_OFF -> R.string.action_screen_off
        ActionId.LAST_APP -> R.string.action_last_app
        ActionId.KILL_APP -> R.string.action_kill_app
        ActionId.MEDIA_PLAY_PAUSE -> R.string.action_media_play_pause
        ActionId.TOGGLE_TORCH -> R.string.action_toggle_torch
        ActionId.VOLUME_PANEL -> R.string.action_volume_panel
        ActionId.CLEAR_NOTIFICATIONS -> R.string.action_clear_notifications
        ActionId.NOTIFICATION_PANEL -> R.string.action_notification_panel
        ActionId.QS_PANEL -> R.string.action_qs_panel
        ActionId.RINGER_MODE -> R.string.action_ringer_mode
    }

@Preview(showBackground = true)
@Composable
private fun ActionPickerScreenPreview() {
    AppTheme(useDynamicColor = false) {
        ActionPickerScreen(
            selectedAction = ActionId.SCREENSHOT,
            onActionChange = {},
            onBack = {},
        )
    }
}
