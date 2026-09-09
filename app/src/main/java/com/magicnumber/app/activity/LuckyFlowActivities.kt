package com.magicnumber.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicnumber.app.domain.magic.MagicEngine
import com.magicnumber.app.ui.components.MagicButton
import com.magicnumber.app.ui.components.MagicPage
import com.magicnumber.app.ui.components.NumberBall
import com.magicnumber.app.ui.theme.MagicGold
import com.magicnumber.app.ui.theme.MagicMuted
import com.magicnumber.app.ui.theme.MagicNumberTheme
import com.magicnumber.app.ui.theme.MagicSurface
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val EXTRA_SELECTED_NUMBERS = "selected_numbers"
private const val EXTRA_COMBINATION_SIZE = "combination_size"
private const val EXTRA_LUCKY_COUNT = "lucky_count"
private const val EXTRA_TOTAL_COMBINATIONS = "total_combinations"
private const val EXTRA_LUCKY_COMBINATIONS = "lucky_combinations"
private const val EXTRA_MAGIC_DATE = "magic_date"

private fun ComponentActivity.magicContentV2(content: @Composable () -> Unit) {
    setContent { MagicNumberTheme { content() } }
}

class LuckyBiometricActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val numbers = intent.getIntegerArrayListExtra(EXTRA_SELECTED_NUMBERS)?.distinct()?.sorted().orEmpty()
        val combinationSize = intent.getIntExtra(EXTRA_COMBINATION_SIZE, minOf(6, numbers.size.coerceAtLeast(1)))
        val luckyCount = intent.getIntExtra(EXTRA_LUCKY_COUNT, 4)
        val total = intent.getLongExtra(EXTRA_TOTAL_COMBINATIONS, 0L)
        val today = LocalDate.now()

        magicContentV2 {
            MagicPage("Attiva la magia", "La tua richiesta è pronta per essere sigillata sul dispositivo") {
                Text("◎", fontSize = 118.sp, color = Color(0xFF43D9FF))
                Spacer(Modifier.height(10.dp))
                Text(
                    "L'impronta sarà il gesto di conferma. Il risultato viene calcolato con UUID, data e configurazione.",
                    color = MagicMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )
                Spacer(Modifier.height(24.dp))
                MagicSummaryCard("Set", "${numbers.size} numeri")
                Spacer(Modifier.height(10.dp))
                MagicSummaryCard("Combinazione", "$combinationSize numeri")
                Spacer(Modifier.height(10.dp))
                MagicSummaryCard("Combinazioni fortunate", luckyCount.toString())
                Spacer(Modifier.height(10.dp))
                MagicSummaryCard("Universo possibile", total.toString())
                Spacer(Modifier.height(26.dp))

                MagicButton("ATTIVA E GENERA") {
                    val combinations = MagicEngine.luckyCombinations(
                        context = this@LuckyBiometricActivity,
                        numbers = numbers,
                        combinationSize = combinationSize,
                        requestedCount = luckyCount,
                        date = today
                    )
                    startActivity(Intent(this@LuckyBiometricActivity, LuckyAnimationActivity::class.java).apply {
                        putStringArrayListExtra(EXTRA_LUCKY_COMBINATIONS, ArrayList(combinations.map { it.joinToString(",") }))
                        putExtra(EXTRA_MAGIC_DATE, today.toString())
                        putExtra(EXTRA_COMBINATION_SIZE, combinationSize)
                        putExtra(EXTRA_LUCKY_COUNT, combinations.size)
                    })
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Stessa installazione + stessa data + stesso set + stessa dimensione = stesso risultato.",
                    color = MagicMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

class LuckyAnimationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val combinations = readCombinations(intent)
        val date = intent.getStringExtra(EXTRA_MAGIC_DATE) ?: LocalDate.now().toString()

        magicContentV2 {
            MagicPage("La magia è in corso…", "Il tuo universo numerico sta prendendo forma") {
                Text("✦", fontSize = 112.sp, color = MagicGold)
                Spacer(Modifier.height(18.dp))
                ProgressLineV2("✓", "Identità installazione")
                ProgressLineV2("✓", "Data del giorno")
                ProgressLineV2("✓", "Calcolo universo combinatorio")
                ProgressLineV2("✓", "Selezione deterministica")
                Spacer(Modifier.height(20.dp))

                if (combinations.isNotEmpty()) {
                    Text("ANTEPRIMA ENERGIA", color = MagicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                    Spacer(Modifier.height(12.dp))
                    NumberStrip(combinations.first())
                }

                Spacer(Modifier.height(28.dp))
                MagicButton("RIVELA LE COMBINAZIONI") {
                    startActivity(Intent(this@LuckyAnimationActivity, LuckyResultsActivity::class.java).apply {
                        putStringArrayListExtra(EXTRA_LUCKY_COMBINATIONS, intent.getStringArrayListExtra(EXTRA_LUCKY_COMBINATIONS))
                        putExtra(EXTRA_MAGIC_DATE, date)
                    })
                }
            }
        }
    }
}

class LuckyResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val combinations = readCombinations(intent)
        val date = runCatching { LocalDate.parse(intent.getStringExtra(EXTRA_MAGIC_DATE)) }.getOrElse { LocalDate.now() }
        val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)

        magicContentV2 {
            MagicPage(
                "Le tue ${combinations.size} combinazioni fortunate",
                date.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString() }
            ) {
                combinations.forEachIndexed { index, combination ->
                    LuckyResultCard(index + 1, combination)
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(12.dp))
                MagicButton("NUOVA SESSIONE") {
                    startActivity(Intent(this@LuckyResultsActivity, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    finish()
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Le combinazioni sono generate per intrattenimento e non prevedono estrazioni reali.",
                    color = MagicMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun readCombinations(intent: Intent): List<List<Int>> =
    intent.getStringArrayListExtra(EXTRA_LUCKY_COMBINATIONS).orEmpty().mapNotNull { encoded ->
        encoded.split(',').mapNotNull(String::toIntOrNull).takeIf { it.isNotEmpty() }
    }

@Composable
private fun MagicSummaryCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MagicSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(17.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MagicMuted)
            Text(value, color = MagicGold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProgressLineV2(symbol: String, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(symbol, color = MagicGold, fontWeight = FontWeight.Black)
        Text(label)
    }
}

@Composable
private fun NumberStrip(numbers: List<Int>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        numbers.take(6).forEach { NumberBall(it, highlighted = true) }
    }
}

@Composable
private fun LuckyResultCard(rank: Int, numbers: List<Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MagicSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("FORTUNATA #$rank", color = MagicGold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                numbers.joinToString("  ·  ") { it.toString().padStart(2, '0') },
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
