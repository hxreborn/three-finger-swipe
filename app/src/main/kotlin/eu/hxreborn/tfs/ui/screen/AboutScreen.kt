package eu.hxreborn.tfs.ui.screen

import android.content.Intent
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

private const val SEPARATOR = " \u00b7 "

@Composable
fun AboutScreen(
    xposedActive: Boolean,
    onNavigateToLicenses: () -> Unit,
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

    SettingsDetailScaffold(
        title = stringResource(R.string.category_about),
        onBack = onBack,
    ) {
        // App header
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

        AboutCard(
            icon = Icons.Outlined.Extension,
            title = stringResource(R.string.about_module_status),
            subtitle =
                if (xposedActive) {
                    stringResource(R.string.about_module_active)
                } else {
                    stringResource(R.string.about_module_inactive)
                },
            shape = shapeForPosition(4, 0),
        )

        Spacer(Modifier.height(2.dp))

        AboutCard(
            icon = Icons.Outlined.Code,
            title = stringResource(R.string.about_source_code),
            subtitle = stringResource(R.string.about_source_code_summary),
            shape = shapeForPosition(4, 1),
            onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/hxreborn/three-finger-swipe".toUri(),
                    ),
                )
            },
        )

        Spacer(Modifier.height(2.dp))

        AboutCard(
            icon = Icons.Outlined.Gavel,
            title = stringResource(R.string.about_licenses),
            subtitle = stringResource(R.string.about_licenses_summary),
            shape = shapeForPosition(4, 2),
            onClick = onNavigateToLicenses,
        )

        Spacer(Modifier.height(2.dp))

        AboutCard(
            icon = Icons.Outlined.BugReport,
            title = stringResource(R.string.about_report_issue),
            subtitle = stringResource(R.string.about_report_issue_summary),
            shape = shapeForPosition(4, 3),
            onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/hxreborn/three-finger-swipe/issues".toUri(),
                    ),
                )
            },
        )
    }
}

@Composable
private fun AboutCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    shape: Shape,
    onClick: (() -> Unit)? = null,
) {
    val rowContent: @Composable () -> Unit = {
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
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) { rowContent() }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    AppTheme(useDynamicColor = false) {
        AboutScreen(
            xposedActive = false,
            onNavigateToLicenses = {},
            onBack = {},
        )
    }
}
