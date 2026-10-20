package eu.hxreborn.tfs.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import eu.hxreborn.tfs.ui.theme.Tokens
import me.zhanghai.compose.preference.PreferenceTheme
import me.zhanghai.compose.preference.preferenceTheme

@Composable
fun sectionPreferenceTheme(): PreferenceTheme =
    preferenceTheme(
        categoryColor = MaterialTheme.colorScheme.primary,
        categoryTextStyle = MaterialTheme.typography.labelLargeEmphasized,
    )

@Composable
fun PreferenceTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
    )
}

fun Modifier.groupedPreference(
    surface: Color,
    shape: Shape,
): Modifier = padding(horizontal = Tokens.ScreenHorizontalPadding).background(color = surface, shape = shape)
