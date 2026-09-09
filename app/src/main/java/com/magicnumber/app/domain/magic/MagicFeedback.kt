package com.magicnumber.app.domain.magic

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Centralized tap feedback (short tone + haptic tick) fired from buttons, steppers, chips and the
 * keypad. Always respects the user's live Settings toggles — no separate "is this real?" state to
 * keep in sync, each call re-reads [SettingsPreferences] directly.
 */
object MagicFeedback {
    private var toneGenerator: ToneGenerator? = null

    /** Sound + vibration feedback for a normal tap (buttons, chips, steppers). */
    fun tap(context: Context) {
        if (SettingsPreferences.isSoundEnabled(context)) playTone()
        if (SettingsPreferences.isVibrationEnabled(context)) vibrate(context, 20L)
    }

    /** Slightly stronger feedback for a confirming action (e.g. keypad "confirm", save, generate). */
    fun confirm(context: Context) {
        if (SettingsPreferences.isSoundEnabled(context)) playTone(ToneGenerator.TONE_PROP_ACK)
        if (SettingsPreferences.isVibrationEnabled(context)) vibrate(context, 35L)
    }

    private fun playTone(tone: Int = ToneGenerator.TONE_PROP_BEEP2) {
        runCatching {
            val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_SYSTEM, 60).also { toneGenerator = it }
            generator.startTone(tone, 40)
        }
    }

    private fun vibrate(context: Context, durationMs: Long) {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
