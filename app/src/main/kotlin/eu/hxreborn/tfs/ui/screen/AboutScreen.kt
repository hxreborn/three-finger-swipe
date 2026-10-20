package eu.hxreborn.tfs.ui.screen

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.ui.component.AdaptiveButtonRow
import eu.hxreborn.tfs.ui.component.InfoRow
import eu.hxreborn.tfs.ui.component.MorphingLogo
import eu.hxreborn.tfs.ui.component.RowTrailing
import eu.hxreborn.tfs.ui.component.SectionHeader
import eu.hxreborn.tfs.ui.component.SettingsRow
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.theme.Tokens
import eu.hxreborn.tfs.ui.util.rememberReleaseNotes
import eu.hxreborn.tfs.ui.viewmodel.Framework

private const val REPO_URL = "https://github.com/hxreborn/three-finger-swipe"
private const val ISSUE_CHOOSER_URL = "$REPO_URL/issues/new/choose"
private const val LICENSE_URL = "$REPO_URL/blob/main/LICENSE"
private const val KOFI_URL = "https://ko-fi.com/hxreborn"
private const val LICENSE = "GPL-3.0"
private val CookieSize = 88.dp

@Composable
fun AboutScreen(
    framework: Framework?,
    onNavigateToLicenses: () -> Unit,
    onNavigateToChangelog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTriggerHotReload: ((String) -> Unit) -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val haptics = LocalHapticFeedback.current
    var showWhatsNew by rememberSaveable { mutableStateOf(false) }

    if (showWhatsNew) {
        WhatsNewSheet(
            onShowChangelog = {
                showWhatsNew = false
                onNavigateToChangelog()
            },
            onDismiss = { showWhatsNew = false },
        )
    }

    SettingsDetailScaffold(
        modifier = modifier,
        title = stringResource(R.string.category_about),
        onBack = onBack,
        gutter = GroupedListGutter,
    ) {
        IdentityCard(onOpenUrl = uriHandler::openUri)

        SectionHeader(stringResource(R.string.about_section_project))
        SettingsRow(
            title = stringResource(R.string.about_whats_new),
            summary = stringResource(R.string.about_whats_new_summary, BuildConfig.VERSION_NAME),
            icon = Icons.Outlined.NewReleases,
            index = 0,
            count = 5,
            trailing = RowTrailing.None,
            onClick = { showWhatsNew = true },
        )
        SettingsRow(
            title = stringResource(R.string.about_changelog),
            summary = stringResource(R.string.about_changelog_summary),
            icon = Icons.Outlined.History,
            index = 1,
            count = 5,
            onClick = onNavigateToChangelog,
        )
        SettingsRow(
            title = stringResource(R.string.about_source_code),
            summary = stringResource(R.string.about_source_code_summary),
            icon = Icons.Outlined.Code,
            index = 2,
            count = 5,
            trailing = RowTrailing.External,
            onClick = { uriHandler.openUri(REPO_URL) },
        )
        SettingsRow(
            title = stringResource(R.string.about_report_issue),
            summary = stringResource(R.string.about_report_issue_summary),
            icon = Icons.Outlined.Lightbulb,
            index = 3,
            count = 5,
            trailing = RowTrailing.External,
            onClick = { uriHandler.openUri(ISSUE_CHOOSER_URL) },
        )
        SettingsRow(
            title = stringResource(R.string.about_licenses),
            summary = stringResource(R.string.about_licenses_summary),
            icon = Icons.Outlined.Description,
            index = 4,
            count = 5,
            onClick = onNavigateToLicenses,
        )

        SectionHeader(stringResource(R.string.about_section_device))
        val deviceRows = if (BuildConfig.DEBUG) 4 else 3
        InfoRow(
            title = stringResource(R.string.about_android),
            summary = stringResource(R.string.about_android_value, Build.VERSION.RELEASE, Build.VERSION.SDK_INT),
            icon = Icons.Outlined.Android,
            index = 0,
            count = deviceRows,
        )
        InfoRow(
            title = stringResource(R.string.about_framework),
            summary = framework?.label() ?: stringResource(R.string.about_framework_none),
            icon = Icons.Outlined.Extension,
            index = 1,
            count = deviceRows,
        )
        InfoRow(
            title = stringResource(R.string.about_build),
            summary =
                stringResource(
                    R.string.about_build_value,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                    BuildConfig.BUILD_TYPE,
                    BuildConfig.GIT_HASH.ifBlank { stringResource(R.string.about_source_build) },
                ),
            icon = Icons.Outlined.Sell,
            index = 2,
            count = deviceRows,
        )
        if (BuildConfig.DEBUG) {
            SettingsRow(
                title = stringResource(R.string.about_hot_reload),
                summary = stringResource(R.string.about_hot_reload_summary),
                icon = Icons.Outlined.Refresh,
                index = 3,
                count = deviceRows,
                trailing = RowTrailing.None,
                onClick = {
                    onTriggerHotReload { status ->
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IdentityCard(onOpenUrl: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(Tokens.GroupedShapeCornerLarge),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().padding(horizontal = GroupedListGutter),
    ) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MorphingLogo(modifier = Modifier.size(CookieSize)) {
                Box(Modifier.matchParentSize().background(MaterialTheme.colorScheme.primaryContainer))
                Icon(
                    painter = painterResource(R.drawable.ic_app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(R.string.about_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { onOpenUrl("$REPO_URL/releases/tag/v${BuildConfig.VERSION_NAME}") },
                    label = { Text("v${BuildConfig.VERSION_NAME}") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Sell, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                    },
                )
                AssistChip(
                    onClick = { onOpenUrl(LICENSE_URL) },
                    label = { Text(LICENSE) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Balance, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                    },
                )
            }
            AdaptiveButtonRow(count = 2, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) { index, stacked ->
                val shapes =
                    when {
                        stacked -> {
                            ButtonDefaults.shapes()
                        }

                        index == 0 -> {
                            ButtonDefaults.shapes(
                                ButtonGroupDefaults.connectedLeadingButtonShape,
                                ButtonGroupDefaults.connectedLeadingButtonPressShape,
                            )
                        }

                        else -> {
                            ButtonDefaults.shapes(
                                ButtonGroupDefaults.connectedTrailingButtonShape,
                                ButtonGroupDefaults.connectedTrailingButtonPressShape,
                            )
                        }
                    }
                if (index == 0) {
                    Button(onClick = { onOpenUrl(REPO_URL) }, shapes = shapes, modifier = Modifier.heightIn(min = 52.dp)) {
                        ButtonLabel(Icons.Outlined.StarOutline, stringResource(R.string.about_star))
                    }
                } else {
                    FilledTonalButton(onClick = { onOpenUrl(KOFI_URL) }, shapes = shapes, modifier = Modifier.heightIn(min = 52.dp)) {
                        ButtonLabel(Icons.Outlined.Coffee, stringResource(R.string.about_coffee))
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonLabel(
    icon: ImageVector,
    text: String,
) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
    Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhatsNewSheet(
    onShowChangelog: () -> Unit,
    onDismiss: () -> Unit,
) {
    val note =
        rememberReleaseNotes()?.let { notes ->
            notes.firstOrNull { it.versionCode == BuildConfig.VERSION_CODE }
                ?: notes.firstOrNull()
        }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.about_whats_new), style = MaterialTheme.typography.titleLarge)
            if (note != null) ReleaseCard(note)
            FilledTonalButton(onClick = onShowChangelog, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_changelog_open))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    AppTheme(useDynamicColor = false) {
        AboutScreen(
            framework = Framework("LSPosed", "2.0", 102),
            onNavigateToLicenses = {},
            onNavigateToChangelog = {},
            onBack = {},
        )
    }
}
