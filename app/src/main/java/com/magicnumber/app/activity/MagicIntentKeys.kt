package com.magicnumber.app.activity

/**
 * Intent extra keys shared across activities that hand off the user's number set
 * and combination configuration to the biometric/lucky flow. Kept in one place so
 * the producer (CombinationConfigActivity) and consumer (LuckyBiometricActivity)
 * can't drift apart.
 */
internal object MagicIntentKeys {
    const val SELECTED_NUMBERS = "selected_numbers"
    const val COMBINATION_SIZE = "combination_size"
    const val LUCKY_COUNT = "lucky_count"
    const val TOTAL_COMBINATIONS = "total_combinations"
}
