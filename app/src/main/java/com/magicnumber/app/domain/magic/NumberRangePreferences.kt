package com.magicnumber.app.domain.magic

import android.content.Context

/**
 * Persists the user's preferred number range (min/max) across sessions.
 * Defaults to 1-90 (classic lottery range) until the user changes it in Impostazioni.
 */
object NumberRangePreferences {
    private const val PREFS = "magicnumber_settings"
    private const val KEY_MIN = "range_min"
    private const val KEY_MAX = "range_max"
    const val DEFAULT_MIN = 1
    const val DEFAULT_MAX = 90

    fun getMin(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_MIN, DEFAULT_MIN)

    fun getMax(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_MAX, DEFAULT_MAX)

    fun setRange(context: Context, min: Int, max: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_MIN, min)
            .putInt(KEY_MAX, max)
            .apply()
    }
}
