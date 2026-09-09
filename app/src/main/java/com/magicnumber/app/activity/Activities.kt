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
import com.magicnumber.app.ui.components.MagicActionCard
import com.magicnumber.app.ui.components.MagicButton
import com.magicnumber.app.ui.components.MagicPage
import com.magicnumber.app.ui.components.NumberBall
import com.magicnumber.app.ui.theme.MagicGold
import com.magicnumber.app.ui.theme.MagicMuted
import com.magicnumber.app.ui.theme.MagicNumberTheme
import com.magicnumber.app.ui.theme.MagicSurface

private val mockNumbers = listOf(4, 11, 17, 29, 36, 44, 53, 67, 74, 88)
private val manualNumbers = listOf(3, 7, 12, 18, 25, 34, 48, 61, 72, 89)

private inline fun <reified T : ComponentActivity> ComponentActivity.open() {
    startActivity(Intent(this, T::class.java))
}

private fun ComponentActivity.magicContent(content: @Composable () -> Unit) {
    setContent { MagicNumberTheme { content() } }
}

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("NUMBER MAGIC", "I tuoi numeri. La tua fortuna.") {
                Text("∞", fontSize = 100.sp, color = MagicGold)
                Spacer(Modifier.height(18.dp))
                Text("Scienza, logica e un pizzico di magia.", color = MagicMuted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(36.dp))
                MagicButton("INIZIA IL TUO VIAGGIO") { open<HomeActivity>() }
            }
        }
    }
}

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Cosa vuoi fare oggi?", "Scegli il tuo percorso numerico") {
                MagicActionCard("Combinazioni fortunate", "Trova le tue combinazioni di oggi", "★") { open<NumberSourceActivity>() }
                Spacer(Modifier.height(12.dp))
                MagicActionCard("Genera i miei numeri", "Crea un set personale", "◆") { open<GenerateNumbersActivity>() }
                Spacer(Modifier.height(12.dp))
                MagicActionCard("Inserisci i miei numeri", "Digita manualmente il tuo set", "⌨") { open<ManualNumbersActivity>() }
                Spacer(Modifier.height(12.dp))
                MagicActionCard("Tutte le combinazioni", "Prepara il set per il combinatore", "▦") { open<ManualNumbersActivity>() }
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
            MagicPage("Scegli come ottenere i tuoi numeri", "Puoi generarli oppure inserirli tu") {
                MagicActionCard("Genera i numeri per me", "Il sistema prepara il set iniziale", "◆") { open<GenerateNumbersActivity>() }
                Spacer(Modifier.height(18.dp))
                MagicActionCard("Inserisco i miei numeri", "Usa il tastierino manuale", "⌨") { open<ManualNumbersActivity>() }
            }
        }
    }
}

class GenerateNumbersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Genera i tuoi numeri", "Configurazione mock della prima milestone") {
                InfoCard("Intervallo", "1 – 90")
                Spacer(Modifier.height(12.dp))
                InfoCard("Quanti numeri generare?", "10")
                Spacer(Modifier.height(30.dp))
                MagicButton("GENERA IL MIO SET") { open<GeneratedNumbersActivity>() }
            }
        }
    }
}

class GeneratedNumbersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage("Ecco i tuoi 10 numeri!", "Set dimostrativo, il motore arriverà nello step dedicato") {
                NumberGrid(mockNumbers, highlighted = true)
                Spacer(Modifier.height(28.dp))
                MagicButton("USA QUESTI NUMERI") { open<CombinationConfigActivity>() }
                Spacer(Modifier.height(10.dp))
                MagicButton("GENERA UN NUOVO SET", { open<GenerateNumbersActivity>() }, secondary = true)
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
