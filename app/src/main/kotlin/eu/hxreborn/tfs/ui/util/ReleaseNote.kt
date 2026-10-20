package eu.hxreborn.tfs.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val NOTES_DIR = "release_notes"
private const val STABLE_BUILD = 9

@Immutable
data class ReleaseNote(
    val versionCode: Int,
    val text: String,
) {
    val versionName: String
        get() = "${versionCode / 10000}.${versionCode / 100 % 100}.${versionCode / 10 % 10}"

    val isPreRelease: Boolean
        get() = versionCode % 10 != STABLE_BUILD
}

private fun loadReleaseNotes(context: Context): List<ReleaseNote> {
    val assets = context.assets
    return assets
        .list(NOTES_DIR)
        .orEmpty()
        .mapNotNull { name ->
            val code = name.removeSuffix(".txt").toIntOrNull() ?: return@mapNotNull null
            val text = assets.open("$NOTES_DIR/$name").bufferedReader().use { it.readText().trim() }
            ReleaseNote(code, text)
        }.sortedByDescending { it.versionCode }
}

@Composable
fun rememberReleaseNotes(): List<ReleaseNote>? {
    val context = LocalContext.current
    return produceState<List<ReleaseNote>?>(initialValue = null, context) {
        value = withContext(Dispatchers.IO) { runCatching { loadReleaseNotes(context) }.getOrDefault(emptyList()) }
    }.value
}
