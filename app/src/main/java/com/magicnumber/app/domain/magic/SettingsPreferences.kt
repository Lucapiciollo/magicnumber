package com.magicnumber.app.domain.magic

import android.content.Context

/**
 * Persists the user-facing toggles shown on the Settings screen (Suoni / Vibrazione /
 * Effetti grafici / Animazioni). Shares the same preferences file as [NumberRangePreferences]
 * so "Ripristina dati" can wipe every app setting back to its default with one call.
 */
object SettingsPreferences {
    private const val PREFS = "magicnumber_settings"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_VIBRATION = "vibration_enabled"
    private const val KEY_EFFECTS = "effects_enabled"
    private const val KEY_ANIMATIONS = "animations_enabled"

    fun isSoundEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_SOUND, true)
    fun setSoundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    fun isVibrationEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_VIBRATION, true)
    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    fun isEffectsEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_EFFECTS, true)
    fun setEffectsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_EFFECTS, enabled).apply()
    }

    fun isAnimationsEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ANIMATIONS, true)
    fun setAnimationsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
    }

    /** Resets every persisted setting (sound/vibration/effects/animations + number range) to its default. */
    fun resetAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
