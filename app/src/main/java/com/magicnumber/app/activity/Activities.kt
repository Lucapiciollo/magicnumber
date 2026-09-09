package com.magicnumber.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicnumber.app.ui.components.AnimatedMagicBackdrop
import com.magicnumber.app.ui.components.FeaturedMagicCard
import com.magicnumber.app.ui.components.MagicActionCard
import com.magicnumber.app.ui.components.MagicButton
import com.magicnumber.app.ui.components.MagicLogoOrb
import com.magicnumber.app.ui.components.MagicPage
import com.magicnumber.app.ui.components.NumberBall
import com.magicnumber.app.ui.theme.MagicGold
import com.magicnumber.app.ui.theme.MagicMuted
import com.magicnumber.app.ui.theme.MagicNumberTheme
import com.magicnumber.app.ui.theme.MagicSurface
import java.util.Random

private const val EXTRA_GENERATED_NUMBERS = "generated_numbers"
private const val EXTRA_MIN = "generation_min"
private const val EXTRA_MAX = "generation_max"
private const val EXTRA_COUNT = "generation_count"

private val manualNumbers = listOf(3, 7, 12, 18, 25, 34, 48, 61, 72, 89)

private inline fun <reified T : ComponentActivity> ComponentActivity.open() {
    startActivity(Intent(this, T::class.java))
}

private fun ComponentActivity.magicContent(content: @Composable () -> Unit) {
    setContent { MagicNumberTheme { content() } }
}

private fun previewGeneratedNumbers(min: Int, max: Int, count: Int): List<Int> {
    val safeMin = min.coerceAtLeast(0)
    val safeMax = max.coerceAtLeast(safeMin)
    val available = safeMax - safeMin + 1
    val safeCount = count.coerceIn(1, available)
    val seed = safeMin * 73_856_093L + safeMax * 19_349_663L + safeCount * 83_492_791L
    val random = Random(seed)
    return (safeMin..safeMax).shuffled(random).take(safeCount).sorted()
}

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            AnimatedMagicBackdrop {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(36.dp))
                    MagicLogoOrb()
                    Spacer(Modifier.height(26.dp))
                    Text(
                        "NUMBER\nMAGIC",
                        fontSize = 42.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "I TUOI NUMERI. LA TUA FORTUNA.",
                        color = MagicGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(22.dp))
                    Text(
                        "Scienza, logica e un pizzico di magia.\nOgni giorno, una combinazione tutta tua.",
                        color = MagicMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.weight(1f))
                    MagicButton("INIZIA IL TUO VIAGGIO") { open<HomeActivity>() }
                    Spacer(Modifier.height(12.dp))
                    Text("OFFLINE · PERSONALE · DETERMINISTICO", color = MagicMuted, fontSize = 10.sp, letterSpacing = 1.2.sp)
                }
            }
        }
    }
}

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Cosa vuoi fare oggi?", "Il tuo universo numerico è pronto") {
                MagicLogoOrb(compact = true)
                Spacer(Modifier.height(14.dp))
                FeaturedMagicCard(
                    title = "Combinazioni fortunate",
                    description = "Parti dai tuoi numeri o lascia che il sistema crei il set di oggi.",
                    action = "ATTIVA LA MAGIA  →",
                    onClick = { open<NumberSourceActivity>() }
                )
                Spacer(Modifier.height(16.dp))
                MagicActionCard("Genera i miei numeri", "Crea il tuo set personale del giorno", "◆") { open<GenerateNumbersActivity>() }
                Spacer(Modifier.height(12.dp))
                MagicActionCard("Inserisci i miei numeri", "Digita manualmente il tuo set", "⌨") { open<ManualNumbersActivity>() }
                Spacer(Modifier.height(12.dp))
                MagicActionCard("Tutte le combinazioni", "Esplora tutte le combinazioni possibili", "▦") { open<ManualNumbersActivity>() }
                Spacer(Modifier.height(12.dp))
                MagicActionCard("Le mie sessioni", "Rivedi risultati e set salvati", "◷") { open<HistoryActivity>() }
                Spacer(Modifier.height(12.dp))
                MagicActionCard("Impostazioni", "Suoni, vibrazione ed effetti", "⚙") { open<SettingsActivity>() }
            }
        }
    }
}

class NumberSourceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            AnimatedMagicBackdrop {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MagicLogoOrb(compact = true)
                    Spacer(Modifier.height(20.dp))
                    Text("DA DOVE PARTIAMO?", color = MagicGold, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Scegli l'origine dei tuoi numeri", fontWeight = FontWeight.Black, fontSize = 28.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Text("Puoi affidarti al sistema oppure usare il tuo set personale.", color = MagicMuted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(34.dp))
                    FeaturedMagicCard(
                        title = "Generali per me",
                        description = "Scegli intervallo e quantità. Il sistema prepara il set iniziale.",
                        action = "GENERA IL SET  →",
                        onClick = { open<GenerateNumbersActivity>() }
                    )
                    Spacer(Modifier.height(16.dp))
                    MagicActionCard("Inserisco i miei numeri", "Usa il tastierino numerico e crea il tuo set", "⌨") { open<ManualNumbersActivity>() }
                }
            }
        }
    }
}

class GenerateNumbersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            var min by remember { mutableIntStateOf(1) }
            var max by remember { mutableIntStateOf(90) }
            var count by remember { mutableIntStateOf(10) }

            val available = (max - min + 1).coerceAtLeast(1)
            val safeCount = count.coerceIn(1, available)
            if (safeCount != count) count = safeCount

            MagicPage("Genera i tuoi numeri", "Configura il set che diventerà la base della tua magia") {
                Text("INTERVALLO", color = MagicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                Spacer(Modifier.height(10.dp))
                StepperCard("Numero minimo", min, canDecrease = min > 0, canIncrease = min < max) { delta ->
                    min = (min + delta).coerceIn(0, max)
                }
                Spacer(Modifier.height(12.dp))
                StepperCard("Numero massimo", max, canDecrease = max > min, canIncrease = max < 999) { delta ->
                    max = (max + delta).coerceIn(min, 999)
                }
                Spacer(Modifier.height(26.dp))
                Text("QUANTITÀ DEL SET", color = MagicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                Spacer(Modifier.height(10.dp))
                StepperCard("Numeri da generare", count, canDecrease = count > 1, canIncrease = count < available) { delta ->
                    count = (count + delta).coerceIn(1, available)
                }
                Spacer(Modifier.height(18.dp))
                InfoCard("Possibili valori distinti", available.toString())
                Spacer(Modifier.height(28.dp))
                MagicButton("GENERA IL MIO SET") {
                    val generated = previewGeneratedNumbers(min, max, count)
                    startActivity(Intent(this@GenerateNumbersActivity, GeneratedNumbersActivity::class.java).apply {
                        putIntegerArrayListExtra(EXTRA_GENERATED_NUMBERS, ArrayList(generated))
                        putExtra(EXTRA_MIN, min)
                        putExtra(EXTRA_MAX, max)
                        putExtra(EXTRA_COUNT, count)
                    })
                }
                Spacer(Modifier.height(10.dp))
                Text("Preview locale · il motore UUID + data verrà collegato nello step dedicato", color = MagicMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

class GeneratedNumbersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val numbers = intent.getIntegerArrayListExtra(EXTRA_GENERATED_NUMBERS)?.toList()
            ?: previewGeneratedNumbers(1, 90, 10)
        val min = intent.getIntExtra(EXTRA_MIN, 1)
        val max = intent.getIntExtra(EXTRA_MAX, 90)

        magicContent {
            MagicPage("Il tuo set è pronto", "${numbers.size} numeri generati nell'intervallo $min – $max") {
                Text("SET GENERATO", color = MagicGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                Spacer(Modifier.height(14.dp))
                NumberGrid(numbers, highlighted = true)
                Spacer(Modifier.height(22.dp))
                InfoCard("Numeri unici", numbers.size.toString())
                Spacer(Modifier.height(12.dp))
                InfoCard("Intervallo", "$min – $max")
                Spacer(Modifier.height(28.dp))
                MagicButton("USA QUESTI NUMERI") { open<CombinationConfigActivity>() }
                Spacer(Modifier.height(10.dp))
                MagicButton("CAMBIA CONFIGURAZIONE", { finish() }, secondary = true)
            }
        }
    }
}

class ManualNumbersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Inserisci i tuoi 10 numeri", "Il tastierino è già predisposto per lo step funzionale") {
                NumberGrid(manualNumbers)
                Spacer(Modifier.height(24.dp))
                KeypadMock()
                Spacer(Modifier.height(24.dp))
                MagicButton("CONFERMA NUMERI") { open<CombinationConfigActivity>() }
            }
        }
    }
}

class CombinationConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Configura le combinazioni fortunate", "Definisci forma e quantità del risultato") {
                NumberGrid(manualNumbers)
                Spacer(Modifier.height(24.dp))
                InfoCard("Numeri per combinazione", "6 · Sestina")
                Spacer(Modifier.height(12.dp))
                InfoCard("Combinazioni fortunate", "4")
                Spacer(Modifier.height(12.dp))
                InfoCard("Combinazioni possibili", "210")
                Spacer(Modifier.height(28.dp))
                MagicButton("PROCEDI") { open<BiometricMagicActivity>() }
            }
        }
    }
}

class BiometricMagicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Attiva la magia", "L'autenticazione biometrica reale verrà collegata nello step dedicato") {
                Text("◎", fontSize = 120.sp, color = Color(0xFF43D9FF))
                Spacer(Modifier.height(16.dp))
                Text("Appoggia il dito per generare le combinazioni fortunate di oggi.", textAlign = TextAlign.Center, color = MagicMuted)
                Spacer(Modifier.height(32.dp))
                MagicButton("SIMULA IMPRONTA") { open<MagicAnimationActivity>() }
            }
        }
    }
}

class MagicAnimationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("La magia è in corso…", "Prima versione della sequenza di elaborazione") {
                Text("✦", fontSize = 110.sp, color = MagicGold)
                Spacer(Modifier.height(22.dp))
                ProgressLine("✓", "Analisi dei numeri")
                ProgressLine("✓", "Calcolo combinazioni")
                ProgressLine("✓", "Selezione deterministica")
                ProgressLine("○", "Rivelazione")
                Spacer(Modifier.height(30.dp))
                MagicButton("MOSTRA RISULTATI") { open<MagicResultsActivity>() }
            }
        }
    }
}

class MagicResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Le tue 4 combinazioni fortunate", "Risultati mock della milestone navigazione") {
                ResultRow("1", listOf(3, 12, 18, 34, 61, 89))
                ResultRow("2", listOf(3, 25, 34, 48, 72, 89))
                ResultRow("3", listOf(7, 18, 25, 34, 61, 72))
                ResultRow("4", listOf(12, 18, 34, 48, 61, 89))
                Spacer(Modifier.height(20.dp))
                MagicButton("NUOVA SESSIONE") {
                    startActivity(Intent(this@MagicResultsActivity, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                }
            }
        }
    }
}

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Le mie sessioni", "Lo storico locale arriverà con Room") {
                InfoCard("Nessuna sessione salvata", "Completa una generazione e salvala qui.")
            }
        }
    }
}

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Impostazioni", "Preferenze dell'esperienza gamer") {
                InfoCard("Suoni", "ON")
                Spacer(Modifier.height(12.dp))
                InfoCard("Vibrazione", "ON")
                Spacer(Modifier.height(12.dp))
                InfoCard("Effetti visivi", "ON")
                Spacer(Modifier.height(12.dp))
                InfoCard("Animazioni", "Complete")
            }
        }
    }
}

@Composable
private fun NumberGrid(numbers: List<Int>, highlighted: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        numbers.chunked(5).forEach { rowNumbers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowNumbers.forEach { NumberBall(it, highlighted) }
            }
        }
    }
}

@Composable
private fun StepperCard(label: String, value: Int, canDecrease: Boolean, canIncrease: Boolean, onStep: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MagicSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, color = MagicMuted, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(value.toString(), color = MagicGold, fontWeight = FontWeight.Black, fontSize = 28.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StepButton("−", canDecrease) { onStep(-1) }
                StepButton("+", canIncrease) { onStep(1) }
            }
        }
    }
}

@Composable
private fun StepButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    MagicButton(
        text = symbol,
        onClick = { if (enabled) onClick() },
        secondary = !enabled
    )
}

@Composable
private fun KeypadMock() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("←", "0", "✓")).forEach { keys ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                keys.forEach { key ->
                    Card(
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MagicSurface)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().height(52.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(key, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MagicSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MagicMuted)
            Text(value, fontWeight = FontWeight.Bold, color = MagicGold)
        }
    }
}

@Composable
private fun ProgressLine(symbol: String, label: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(symbol, color = MagicGold, fontWeight = FontWeight.Bold)
        Text(label)
    }
}

@Composable
private fun ResultRow(rank: String, numbers: List<Int>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MagicSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("FORTUNATA #$rank", color = MagicGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Text(numbers.joinToString("  ·  ") { it.toString().padStart(2, '0') }, fontWeight = FontWeight.Bold)
        }
    }
}
