package eu.hxreborn.tfs.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val NOTES_DIR = "release_notes"
private const val DATES_FILE = "dates.txt"
private const val STABLE_BUILD = 9

@Immutable
data class ReleaseNote(
    val versionCode: Int,
    val text: String,
    val releasedOn: LocalDate? = null,
) {
    @Immutable
    data class Line(
        val kind: Kind,
        val text: String,
    ) {
        enum class Kind(
            val verbs: Set<String>,
        ) {
            NEW(setOf("added", "add")),
            FIXED(setOf("fixed", "fix")),
            CHANGED(setOf("changed", "change", "switched", "moved", "renamed", "reworked", "rebuilt")),
            UPDATED(setOf("updated", "update", "bumped")),
            REMOVED(setOf("removed", "remove", "dropped")),
            OTHER(emptySet()),
            ;

            companion object {
                fun of(text: String): Kind {
                    val verb = text.substringBefore(' ').lowercase()
                    return entries.firstOrNull { verb in it.verbs } ?: OTHER
                }
            }
        }
    }

    val versionName: String
        get() = "${versionCode / 10000}.${versionCode / 100 % 100}.${versionCode / 10 % 10}"

    val isPreRelease: Boolean
        get() = versionCode % 10 != STABLE_BUILD

    val lines: List<Line>
        get() =
            text
                .lines()
                .map { it.trim().removePrefix("- ").trim() }
                .filter { it.isNotEmpty() }
                .map { Line(Line.Kind.of(it), it) }
                .sortedBy { it.kind.ordinal }
}

private fun loadReleaseNotes(context: Context): List<ReleaseNote> {
    val assets = context.assets
    val names = assets.list(NOTES_DIR).orEmpty()
    val dates =
        if (DATES_FILE in names) {
            assets
                .open("$NOTES_DIR/$DATES_FILE")
                .bufferedReader()
                .useLines { lines ->
                    lines
                        .mapNotNull { line ->
                            val (code, date) = line.split(' ').takeIf { it.size == 2 } ?: return@mapNotNull null
                            code.toIntOrNull()?.let { it to LocalDate.parse(date) }
                        }.toMap()
                }
        } else {
            emptyMap()
        }
    return names
        .mapNotNull { name ->
            val code = name.removeSuffix(".txt").toIntOrNull() ?: return@mapNotNull null
            val text = assets.open("$NOTES_DIR/$name").bufferedReader().use { it.readText().trim() }
            ReleaseNote(code, text, dates[code])
        }.sortedByDescending { it.versionCode }
}

@Composable
fun rememberReleaseNotes(): List<ReleaseNote>? {
    val context = LocalContext.current
    return produceState<List<ReleaseNote>?>(initialValue = null, context) {
        value = withContext(Dispatchers.IO) { runCatching { loadReleaseNotes(context) }.getOrDefault(emptyList()) }
    }.value
}
