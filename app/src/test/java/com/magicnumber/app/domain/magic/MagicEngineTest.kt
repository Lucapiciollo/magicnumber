package com.magicnumber.app.domain.magic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rete di sicurezza per [MagicEngine] prima di rifattorizzare i cicli pesanti
 * (`luckyCombinations`, `combinationAt`) verso l'esecuzione in coroutine.
 *
 * Nota: [MagicEngine.luckyCombinations] richiede un [android.content.Context] per
 * ottenere/creare un UUID di installazione persistito in SharedPreferences, quindi
 * qui viene testata solo la parte pura e deterministica dell'algoritmo:
 * [MagicEngine.combinationsCount] e la logica combinatoria esposta indirettamente
 * tramite [MagicEngine.combinationsCount].
 */
class MagicEngineTest {

    @Test
    fun `combinationsCount di casi base noti`() {
        assertEquals(1L, MagicEngine.combinationsCount(0, 0))
        assertEquals(1L, MagicEngine.combinationsCount(5, 0))
        assertEquals(1L, MagicEngine.combinationsCount(5, 5))
        assertEquals(5L, MagicEngine.combinationsCount(5, 1))
        assertEquals(10L, MagicEngine.combinationsCount(5, 2))
        assertEquals(10L, MagicEngine.combinationsCount(5, 3))
        assertEquals(117480L, MagicEngine.combinationsCount(90, 3))
    }

    @Test
    fun `combinationsCount con input non validi ritorna 0`() {
        assertEquals(0L, MagicEngine.combinationsCount(-1, 2))
        assertEquals(0L, MagicEngine.combinationsCount(5, -1))
        assertEquals(0L, MagicEngine.combinationsCount(3, 5))
    }

    @Test
    fun `combinationsCount e simmetrica su k e n-k`() {
        val n = 49
        for (k in 0..n) {
            assertEquals(
                "C($n,$k) deve essere uguale a C($n,${n - k})",
                MagicEngine.combinationsCount(n, k),
                MagicEngine.combinationsCount(n, n - k)
            )
        }
    }

    @Test
    fun `combinationsCount satura a Long MAX_VALUE senza overflow negativo`() {
        val result = MagicEngine.combinationsCount(1000, 500)
        assertTrue("il risultato non deve mai diventare negativo per overflow", result > 0)
    }

    @Test
    fun `combinationAt indice zero restituisce la prima combinazione in ordine lessicografico`() {
        val numbers = (1..10).toList()
        assertEquals(listOf(1, 2, 3), MagicEngine.combinationAt(numbers, 3, 0))
    }

    @Test
    fun `combinationAt ultimo indice restituisce l ultima combinazione in ordine lessicografico`() {
        val numbers = (1..6).toList()
        val total = MagicEngine.combinationsCount(numbers.size, 3)
        assertEquals(listOf(4, 5, 6), MagicEngine.combinationAt(numbers, 3, total - 1))
    }

    @Test
    fun `combinationAt genera tutte le combinazioni distinte senza duplicati su tutto il range`() {
        val numbers = (1..8).toList()
        val k = 3
        val total = MagicEngine.combinationsCount(numbers.size, k)
        val all = (0 until total).map { MagicEngine.combinationAt(numbers, k, it) }
        assertEquals("ogni indice deve mappare a una combinazione unica", total.toInt(), all.distinct().size)
        all.forEach { combo ->
            assertEquals("ogni combinazione deve essere ordinata", combo.sorted(), combo)
            assertEquals("ogni combinazione non deve avere ripetizioni", k, combo.distinct().size)
        }
    }
}
