package eu.hxreborn.tfs.prefs

import android.content.SharedPreferences

sealed class PrefSpec<T : Any>(
    val key: String,
    val default: T,
) {
    abstract fun read(prefs: SharedPreferences): T

    abstract fun write(
        editor: SharedPreferences.Editor,
        value: T,
    )

    fun reset(editor: SharedPreferences.Editor) = write(editor, default)

    // an unchanged key must not fire the cross-process listener
    fun copyIfChanged(
        from: SharedPreferences,
        to: SharedPreferences,
        editor: SharedPreferences.Editor,
    ): Boolean {
        val value = read(from)
        if (read(to) == value) return false
        write(editor, value)
        return true
    }
}

class IntPref(
    key: String,
    default: Int,
    private val range: IntRange? = null,
    val step: Int? = null,
) : PrefSpec<Int>(key, default) {
    init {
        require(step == null || step > 0) { "step must be positive" }
    }

    val sliderRange: ClosedFloatingPointRange<Float>? =
        range?.let { it.first.toFloat()..it.last.toFloat() }
    val sliderSteps: Int =
        when {
            step == null || range == null -> 0
            else -> ((range.last - range.first) / step) - 1
        }.coerceAtLeast(0)

    override fun read(prefs: SharedPreferences): Int {
        val raw = prefs.getInt(key, default)
        return range?.let(raw::coerceIn) ?: raw
    }

    override fun write(
        editor: SharedPreferences.Editor,
        value: Int,
    ) {
        editor.putInt(key, range?.let(value::coerceIn) ?: value)
    }
}

fun <T : Any> PrefSpec<T>.readOrDefault(prefs: SharedPreferences?): T =
    prefs?.let { read(it) } ?: default

class StringPref(
    key: String,
    default: String,
) : PrefSpec<String>(key, default) {
    override fun read(prefs: SharedPreferences): String = prefs.getString(key, default) ?: default

    override fun write(
        editor: SharedPreferences.Editor,
        value: String,
    ) {
        editor.putString(key, value)
    }
}

class StringSetPref(
    key: String,
    default: Set<String>,
) : PrefSpec<Set<String>>(key, default) {
    override fun read(prefs: SharedPreferences): Set<String> =
        prefs.getStringSet(key, default) ?: default

    // Kotlin collection classes are absent in the framework process that unparcels this
    override fun write(
        editor: SharedPreferences.Editor,
        value: Set<String>,
    ) {
        editor.putStringSet(key, HashSet(value))
    }
}
