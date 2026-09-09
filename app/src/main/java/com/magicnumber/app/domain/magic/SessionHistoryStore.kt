package com.magicnumber.app.domain.magic

import android.content.Context

/** A previously generated and explicitly saved set of lucky combinations. */
data class SavedSession(
    val timestampMillis: Long,
    val combinationSize: Int,
    val combinations: List<List<Int>>
)

/**
 * Minimal local persistence for "Le mie sessioni" (Storico) — no database dependency needed,
 * a compact delimited string is stored in SharedPreferences. Newest session first, capped to 20.
 */
object SessionHistoryStore {
    private const val PREFS = "magicnumber_history"
    private const val KEY_SESSIONS = "saved_sessions"
    private const val MAX_SESSIONS = 20

    fun save(context: Context, combinationSize: Int, combinations: List<List<Int>>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encoded = encode(SavedSession(System.currentTimeMillis(), combinationSize, combinations))
        val existing = prefs.getString(KEY_SESSIONS, "").orEmpty()
        val updated = (listOf(encoded) + existing.split("\n").filter { it.isNotBlank() }).take(MAX_SESSIONS)
        prefs.edit().putString(KEY_SESSIONS, updated.joinToString("\n")).apply()
    }

    fun loadAll(context: Context): List<SavedSession> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SESSIONS, "").orEmpty()
            .split("\n")
            .filter { it.isNotBlank() }
            .mapNotNull(::decode)
    }

    /** Removes a single saved session, identified by its (effectively unique) timestamp. */
    fun delete(context: Context, timestampMillis: Long) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val remaining = prefs.getString(KEY_SESSIONS, "").orEmpty()
            .split("\n")
            .filter { it.isNotBlank() && decode(it)?.timestampMillis != timestampMillis }
        prefs.edit().putString(KEY_SESSIONS, remaining.joinToString("\n")).apply()
    }

    /** Wipes every saved session. */
    fun deleteAll(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_SESSIONS).apply()
    }

    private fun encode(session: SavedSession): String =
        "${session.timestampMillis}:${session.combinationSize}:${session.combinations.joinToString("|") { it.joinToString("-") }}"

    private fun decode(raw: String): SavedSession? {
        val parts = raw.split(":", limit = 3)
        if (parts.size != 3) return null
        val timestamp = parts[0].toLongOrNull() ?: return null
        val size = parts[1].toIntOrNull() ?: return null
        val combinations = parts[2].split("|").filter { it.isNotBlank() }.map { group ->
            group.split("-").mapNotNull(String::toIntOrNull)
        }
        if (combinations.isEmpty()) return null
        return SavedSession(timestamp, size, combinations)
    }
}
