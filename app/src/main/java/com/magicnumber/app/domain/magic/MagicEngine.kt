package com.magicnumber.app.domain.magic

import android.content.Context
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDate
import java.util.UUID
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Ogni tot iterazioni dei cicli pesanti si cede il thread di sfondo (cooperative yielding). */
private const val YIELD_EVERY = 256

object MagicEngine {
    private const val PREFS = "magicnumber_identity"
    private const val UUID_KEY = "installation_uuid"
    private const val VERSION = "PICK-V1"

    fun installationUuid(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(UUID_KEY, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(UUID_KEY, it).apply()
        }
    }

    /**
     * Calcola le combinazioni fortunate. Esegue il lavoro pesante (hashing SHA-256 ripetuto
     * e ricostruzione delle combinazioni tramite ranking combinatorio) su [Dispatchers.Default],
     * cedendo periodicamente il thread con `yield()`/`ensureActive()` per non bloccare mai la UI
     * e per rispettare l'eventuale cancellazione della coroutine chiamante.
     */
    suspend fun luckyCombinations(
        context: Context,
        numbers: List<Int>,
        combinationSize: Int,
        requestedCount: Int,
        date: LocalDate = LocalDate.now()
    ): List<List<Int>> {
        val normalized = numbers.distinct().sorted()
        require(normalized.isNotEmpty())
        require(combinationSize in 1..normalized.size)

        val total = combinationsCount(normalized.size, combinationSize)
        require(total > 0)
        val count = requestedCount.coerceIn(1, total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())

        val seed = buildString {
            append(VERSION)
            append('|')
            append(installationUuid(context))
            append('|')
            append(date)
            append('|')
            append(normalized.joinToString(","))
            append('|')
            append("K=")
            append(combinationSize)
        }

        return withContext(Dispatchers.Default) {
            val selectedIndexes = linkedSetOf<Long>()
            var counter = 0L
            while (selectedIndexes.size < count) {
                val digest = sha256("$seed|$counter")
                val index = BigInteger(1, digest).mod(BigInteger.valueOf(total)).toLong()
                selectedIndexes += index
                counter++
                if (counter % YIELD_EVERY == 0L) {
                    coroutineContext.ensureActive()
                    kotlinx.coroutines.yield()
                }
            }

            selectedIndexes.map { index ->
                coroutineContext.ensureActive()
                combinationAt(normalized, combinationSize, index)
            }
        }
    }

    fun combinationsCount(n: Int, k: Int): Long {
        if (k < 0 || n < 0 || k > n) return 0L
        if (k == 0 || k == n) return 1L
        val r = minOf(k, n - k)
        var result = 1L
        for (i in 1..r) {
            val numerator = n - r + i
            if (result > Long.MAX_VALUE / numerator) return Long.MAX_VALUE
            result = result * numerator / i
        }
        return result
    }

    /** Visibilità `internal` (anziché `private`) solo per poterla coprire con unit test dedicati. */
    internal fun combinationAt(numbers: List<Int>, k: Int, zeroBasedIndex: Long): List<Int> {
        val n = numbers.size
        var rank = zeroBasedIndex
        val positions = IntArray(k)
        var start = 0

        for (slot in 0 until k) {
            val remainingSlots = k - slot - 1
            var candidate = start
            while (candidate < n) {
                val remainingItems = n - candidate - 1
                val blockSize = if (remainingSlots == 0) 1L else combinationsCount(remainingItems, remainingSlots)
                if (rank < blockSize) {
                    positions[slot] = candidate
                    start = candidate + 1
                    break
                }
                rank -= blockSize
                candidate++
            }
        }

        return positions.map(numbers::get)
    }

    private fun sha256(value: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray(StandardCharsets.UTF_8))
}
