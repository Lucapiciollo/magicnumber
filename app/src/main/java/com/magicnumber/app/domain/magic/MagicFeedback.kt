package com.magicnumber.app.domain.magic

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.magicnumber.app.R

/**
 * Centralized tap feedback (themed sound + haptic tick) fired from buttons, steppers, chips, the
 * keypad and the "magic" generation/reveal flow. Always respects the user's live Settings toggles —
 * no separate "is this real?" state to keep in sync, each call re-reads [SettingsPreferences]
 * directly.
 *
 * Uses [SoundPool] with real bundled sound effects (res/raw) routed through [AudioAttributes.USAGE_GAME]
 * (→ media/game volume stream) rather than [android.media.ToneGenerator] on `STREAM_SYSTEM`, which on
 * many OEM devices (e.g. Samsung, when "touch sounds"/system volume is muted) is silent regardless of
 * app logic.
 */
object MagicFeedback {
    private var soundPool: SoundPool? = null
    private val loadedSounds = HashSet<Int>()
    private var idTap = 0
    private var idConfirm = 0
    private var idGenerateTick = 0
    private var idReveal = 0
    private var releaseOnBackgroundRegistered = false

    private fun pool(context: Context): SoundPool {
        soundPool?.let { return it }
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()
        pool.setOnLoadCompleteListener { _, sampleId, status -> if (status == 0) loadedSounds.add(sampleId) }
        val app = context.applicationContext
        idTap = pool.load(app, R.raw.sfx_tap, 1)
        idConfirm = pool.load(app, R.raw.sfx_confirm, 1)
        idGenerateTick = pool.load(app, R.raw.sfx_generate_tick, 1)
        idReveal = pool.load(app, R.raw.sfx_reveal, 1)
        soundPool = pool
        registerReleaseOnBackground()
        return pool
    }

    /**
     * Il [SoundPool] è una risorsa nativa (decoder audio): la rilasciamo automaticamente quando
     * l'intero processo va in background (nessuna Activity in foreground), evitando che resti
     * allocata per tutta la vita del processo. Viene ricreata alla lazy on-demand (vedi [pool])
     * la prima volta che serve di nuovo un suono.
     */
    private fun registerReleaseOnBackground() {
        if (releaseOnBackgroundRegistered) return
        releaseOnBackgroundRegistered = true
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) = release()
        })
    }

    /** Rilascia il [SoundPool] nativo e i relativi campioni caricati. Sicuro da chiamare più volte. */
    fun release() {
        soundPool?.release()
        soundPool = null
        loadedSounds.clear()
    }

    /** Sound + vibration feedback for a normal tap (buttons, chips, steppers). */
    fun tap(context: Context) {
        if (SettingsPreferences.isSoundEnabled(context)) playSound(context, idTap)
        if (SettingsPreferences.isVibrationEnabled(context)) vibrate(context, 20L)
    }

    /** Slightly stronger feedback for a confirming action (e.g. keypad "confirm", save, generate). */
    fun confirm(context: Context) {
        if (SettingsPreferences.isSoundEnabled(context)) playSound(context, idConfirm)
        if (SettingsPreferences.isVibrationEnabled(context)) vibrate(context, 35L)
    }

    /** Soft twinkle played on each number tick while the "magia in corso" rotation is running. */
    fun generateTick(context: Context) {
        if (SettingsPreferences.isSoundEnabled(context)) playSound(context, idGenerateTick, volume = .55f)
    }

    /** Rewarding ascending chime for the final reveal (combinations ready / shown). */
    fun reveal(context: Context) {
        if (SettingsPreferences.isSoundEnabled(context)) playSound(context, idReveal)
        if (SettingsPreferences.isVibrationEnabled(context)) vibrate(context, 45L)
    }

    private fun playSound(context: Context, soundId: Int, volume: Float = 1f) {
        runCatching {
            val pool = pool(context)
            if (soundId != 0 && loadedSounds.contains(soundId)) {
                pool.play(soundId, volume, volume, 1, 0, 1f)
            }
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
