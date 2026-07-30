package eu.hxreborn.tfs.ui.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import eu.hxreborn.tfs.BuildConfig
import eu.hxreborn.tfs.R
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.util.shapeForPosition

private const val SEPARATOR = " · "
private const val REPO_URL = "https://github.com/hxreborn/three-finger-swipe"

private class AboutEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: (() -> Unit)? = null,
)

@Composable
fun AboutScreen(
    xposedServiceAvailable: Boolean,
    modifier: Modifier = Modifier,
    onNavigateToLicenses: () -> Unit,
    onTriggerHotReload: ((String) -> Unit) -> Unit = {},
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val sourceBuild = stringResource(R.string.about_source_build)
    val versionSubtitle =
        listOf(
            "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            BuildConfig.BUILD_TYPE,
            BuildConfig.GIT_HASH.takeIf { it.isNotBlank() } ?: sourceBuild,
        ).joinToString(SEPARATOR)

    val entries =
        buildList {
            add(
                AboutEntry(
                    icon = Icons.Outlined.Extension,
                    title = stringResource(R.string.about_module_status),
                    subtitle =
                        if (xposedServiceAvailable) {
                            stringResource(R.string.about_module_active)
                        } else {
                            stringResource(R.string.about_module_inactive)
                        },
                ),
            )
            add(
                AboutEntry(
                    icon = Icons.Outlined.Code,
                    title = stringResource(R.string.about_source_code),
                    subtitle = stringResource(R.string.about_source_code_summary),
                    onClick = { context.openUrl(REPO_URL) },
                ),
            )
            add(
                AboutEntry(
                    icon = Icons.Outlined.Gavel,
                    title = stringResource(R.string.about_licenses),
                    subtitle = stringResource(R.string.about_licenses_summary),
                    onClick = onNavigateToLicenses,
                ),
            )
            add(
                AboutEntry(
                    icon = Icons.Outlined.BugReport,
                    title = stringResource(R.string.about_report_issue),
                    subtitle = stringResource(R.string.about_report_issue_summary),
                    onClick = { context.openUrl("$REPO_URL/issues") },
                ),
            )
            if (BuildConfig.DEBUG) {
                add(
                    AboutEntry(
                        icon = Icons.Outlined.Refresh,
                        title = stringResource(R.string.about_hot_reload),
                        subtitle = stringResource(R.string.about_hot_reload_summary),
                        onClick = {
                            onTriggerHotReload { status ->
                                Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                            }
                        },
                    ),
                )
            }
        }

    SettingsDetailScaffold(
        modifier = modifier,
        title = stringResource(R.string.category_about),
        onBack = onBack,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = versionSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(8.dp))

        entries.forEachIndexed { index, entry ->
            if (index > 0) Spacer(Modifier.height(2.dp))
            AboutCard(
                icon = entry.icon,
                title = entry.title,
                subtitle = entry.subtitle,
                shape = shapeForPosition(entries.size, index),
                onClick = entry.onClick,
            )
        }
    }
}

private fun android.content.Context.openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))

@Composable
private fun AboutCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    val cardModifier = modifier.fillMaxWidth()

    if (onClick == null) {
        Surface(
            modifier = cardModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            content = content,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            content = content,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    AppTheme(useDynamicColor = false) {
        AboutScreen(
            xposedServiceAvailable = false,
            onNavigateToLicenses = {},
            onBack = {},
        )
    }
}
