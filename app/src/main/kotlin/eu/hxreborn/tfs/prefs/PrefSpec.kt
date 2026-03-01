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

    fun copyTo(
        from: SharedPreferences,
        to: SharedPreferences.Editor,
    ) = write(to, read(from))
}

class BoolPref(
    key: String,
    default: Boolean,
) : PrefSpec<Boolean>(key, default) {
    override fun read(prefs: SharedPreferences): Boolean = prefs.getBoolean(key, default)

    override fun write(
        editor: SharedPreferences.Editor,
        value: Boolean,
    ) {
        editor.putBoolean(key, value)
    }
}

class IntPref(
    key: String,
    default: Int,
    private val range: IntRange? = null,
) : PrefSpec<Int>(key, default) {
    val sliderRange: ClosedFloatingPointRange<Float>? =
        range?.let { it.first.toFloat()..it.last.toFloat() }

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
