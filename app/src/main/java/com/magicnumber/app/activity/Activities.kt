package com.magicnumber.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicnumber.app.R
import com.magicnumber.app.domain.magic.MagicFeedback
import com.magicnumber.app.domain.magic.MagicEngine
import com.magicnumber.app.domain.magic.NumberRangePreferences
import com.magicnumber.app.domain.magic.SettingsPreferences
import com.magicnumber.app.ui.components.MagicImageBackdrop
import com.magicnumber.app.ui.components.FeaturedMagicCard
import com.magicnumber.app.ui.components.InfoCard

import com.magicnumber.app.ui.components.MagicActionCard
import com.magicnumber.app.ui.components.MagicBottomNav
import com.magicnumber.app.ui.components.MagicButton
import com.magicnumber.app.ui.components.MagicCard
import com.magicnumber.app.ui.components.MagicChip
import com.magicnumber.app.ui.components.MagicChoiceCard
import com.magicnumber.app.ui.components.MagicGlowIcon
import com.magicnumber.app.ui.components.MagicListRow
import com.magicnumber.app.ui.components.MagicPage
import com.magicnumber.app.ui.components.MagicChevronRow
import com.magicnumber.app.ui.components.MagicRowDivider
import com.magicnumber.app.ui.components.MagicSectionTitle
import com.magicnumber.app.ui.components.MagicSlider
import com.magicnumber.app.ui.components.MagicStepper
import com.magicnumber.app.ui.components.NumberBall
import com.magicnumber.app.ui.theme.MagicAmber
import com.magicnumber.app.ui.theme.MagicCyan
import com.magicnumber.app.ui.theme.MagicGold
import com.magicnumber.app.ui.theme.MagicMuted
import com.magicnumber.app.ui.theme.MagicNumberTheme
import com.magicnumber.app.ui.theme.MagicPink
import com.magicnumber.app.ui.theme.MagicPurple
import com.magicnumber.app.ui.theme.MagicSurface
import java.util.Random

private const val EXTRA_GENERATED_NUMBERS = "generated_numbers"
private const val EXTRA_MIN = "generation_min"
private const val EXTRA_MAX = "generation_max"
private const val EXTRA_COUNT = "generation_count"
private val EXTRA_SELECTED_NUMBERS = MagicIntentKeys.SELECTED_NUMBERS
private val EXTRA_COMBINATION_SIZE = MagicIntentKeys.COMBINATION_SIZE
private val EXTRA_LUCKY_COUNT = MagicIntentKeys.LUCKY_COUNT
private val EXTRA_TOTAL_COMBINATIONS = MagicIntentKeys.TOTAL_COMBINATIONS
private val quickPickCounts = listOf(5, 10, 12, 15, 20)

private val manualNumbers = listOf(3, 7, 12, 18, 25, 34, 48, 61, 72, 89)

private inline fun <reified T : ComponentActivity> ComponentActivity.open() {
    startActivity(Intent(this, T::class.java))
}

/** Scaffolding condiviso da tutte le Activity: applica il tema e propaga le preference effetti/animazioni. */
internal fun ComponentActivity.magicContent(content: @Composable () -> Unit) {
    setContent {
        val effectsEnabled = remember { com.magicnumber.app.domain.magic.SettingsPreferences.isEffectsEnabled(this) }
        val animationsEnabled = remember { com.magicnumber.app.domain.magic.SettingsPreferences.isAnimationsEnabled(this) }
        androidx.compose.runtime.CompositionLocalProvider(
            com.magicnumber.app.ui.components.LocalEffectsEnabled provides effectsEnabled,
            com.magicnumber.app.ui.components.LocalAnimationsEnabled provides animationsEnabled
        ) {
            MagicNumberTheme { content() }
        }
    }
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

private fun combinationLabel(k: Int): String = when (k) {
    2 -> "Coppia"
    3 -> "Terzina"
    4 -> "Quartina"
    5 -> "Cinquina"
    6 -> "Sestina"
    else -> "$k numeri"
}

private fun combinationLabelPlural(k: Int): String = when (k) {
    2 -> "coppie"
    3 -> "terzine"
    4 -> "quartine"
    5 -> "cinquine"
    6 -> "sestine"
    else -> "set da $k"
}

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicImageBackdrop(R.drawable.bg_splash) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(40.dp))
                    Text("NUMBER\nMAGIC", fontSize = 42.sp, lineHeight = 42.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, letterSpacing = 2.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("I TUOI NUMERI. LA TUA FORTUNA.", color = MagicGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(260.dp))
                    Text("Scienza, logica e un pizzico di magia.\nOgni giorno, una combinazione tutta tua.", color = MagicMuted, textAlign = TextAlign.Center, lineHeight = 22.sp)
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
            MagicPage(
                title = null,
                background = R.drawable.bg_number_source,
                bottomBar = {
                    MagicBottomNav(
                        selected = "home",
                        onHome = {},
                        onMagic = { open<NumberSourceActivity>() },
                        onHistory = { open<HistoryActivity>() },
                        onMore = { open<SettingsActivity>() }
                    )
                }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("NUMBER MAGIC", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MagicGold, letterSpacing = .5.sp)
                    Text("Il tuo universo numerico", color = MagicMuted, fontSize = 13.sp)
                }
                Spacer(Modifier.height(20.dp))
                FeaturedMagicCard("Combinazioni fortunate", "Trova le tue combinazioni di oggi", "ATTIVA LA MAGIA  →") { open<NumberSourceActivity>() }
                Spacer(Modifier.height(16.dp))
                MagicCard(contentPadding = 6.dp) {
                    MagicListRow("Genera i miei numeri", "◆", subtitle = "Crea un set unico per te", accent = MagicPurple) { open<GenerateNumbersActivity>() }
                    MagicRowDivider()
                    MagicListRow("Inserisci i miei numeri", "⌨", subtitle = "Digita i tuoi numeri preferiti", accent = MagicCyan) { open<ManualNumbersActivity>() }
                    MagicRowDivider()
                    MagicListRow("Tutte le combinazioni", "▦", subtitle = "Esplora tutte le possibilità", accent = MagicAmber) { open<ManualNumbersActivity>() }
                    MagicRowDivider()
                    MagicListRow("Storico", "◷", subtitle = "Le tue sessioni", accent = MagicCyan) { open<HistoryActivity>() }
                    MagicRowDivider()
                    MagicListRow("Impostazioni", "⚙", subtitle = "Personalizza l'app", accent = MagicPurple) { open<SettingsActivity>() }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

class NumberSourceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage(
                title = "Come vuoi ottenere i tuoi numeri?",
                background = R.drawable.bg_number_source
            ) {
                MagicChoiceCard("⚄", "Generali per me", "Crea un set unico basato su data e dispositivo", "GENERA ORA  →") { open<GenerateNumbersActivity>() }
                Spacer(Modifier.height(16.dp))
                MagicActionCard("Inserisco i miei numeri", "Digita manualmente i numeri che preferisci", "✍") { open<ManualNumbersActivity>() }
                Spacer(Modifier.height(24.dp))
                Text(
                    "\u201CLa fortuna non è un caso, è una combinazione.\u201D",
                    color = MagicMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

class GenerateNumbersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            val min = NumberRangePreferences.getMin(this@GenerateNumbersActivity)
            val max = NumberRangePreferences.getMax(this@GenerateNumbersActivity)
            val available = (max - min + 1).coerceAtLeast(1)
            val minCount = minOf(5, available)
            var count by rememberSaveable { mutableIntStateOf(minOf(10, available).coerceAtLeast(minCount)) }
            val safeCount = count.coerceIn(minCount, available)
            if (safeCount != count) count = safeCount

            MagicPage("Genera i tuoi numeri", "Configura il set iniziale", background = R.drawable.bg_number_source) {
                MagicSectionTitle("QUANTI NUMERI GENERARE?")
                Spacer(Modifier.height(10.dp))
                MagicSlider("Numeri da generare", count, minCount..available) {
                    count = it.coerceIn(minCount, available)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickPickCounts.forEach { pick ->
                        MagicChip(pick.toString(), selected = count == pick.coerceIn(minCount, available)) {
                            count = pick.coerceIn(minCount, available)
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
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
                Text("Ogni set è unico, basato su data, ora e sul tuo dispositivo.", color = MagicMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

class GeneratedNumbersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val numbers = intent.getIntegerArrayListExtra(EXTRA_GENERATED_NUMBERS)?.toList() ?: previewGeneratedNumbers(1, 90, 10)
        val min = intent.getIntExtra(EXTRA_MIN, 1)
        val max = intent.getIntExtra(EXTRA_MAX, 90)
        val generatedAt = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy - HH:mm", java.util.Locale.ITALIAN))

        magicContent {
            LaunchedEffect(Unit) { com.magicnumber.app.domain.magic.MagicFeedback.reveal(this@GeneratedNumbersActivity) }
            MagicPage("Ecco i tuoi ${numbers.size} numeri!", null, background = R.drawable.bg_number_source) {
                NumberGrid(numbers, highlighted = true, animate = true)
                Spacer(Modifier.height(22.dp))
                InfoCard("Intervallo", "$min – $max")
                Spacer(Modifier.height(12.dp))
                InfoCard("Numeri generati", numbers.size.toString())
                Spacer(Modifier.height(12.dp))
                InfoCard("Generati il", generatedAt)
                Spacer(Modifier.height(28.dp))
                MagicButton("USA QUESTI NUMERI") {
                    startActivity(Intent(this@GeneratedNumbersActivity, CombinationConfigActivity::class.java).apply {
                        putIntegerArrayListExtra(EXTRA_SELECTED_NUMBERS, ArrayList(numbers))
                    })
                }
                Spacer(Modifier.height(10.dp))
                MagicButton("GENERA UN NUOVO SET", secondary = true) { finish() }
            }
        }
    }
}

class ManualNumbersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            val rangeMin = NumberRangePreferences.getMin(this@ManualNumbersActivity)
            val rangeMax = NumberRangePreferences.getMax(this@ManualNumbersActivity)
            var selectedNumbers by rememberSaveable(
                stateSaver = listSaver<List<Int>, Int>(save = { it }, restore = { it })
            ) { mutableStateOf(emptyList()) }
            var currentInput by rememberSaveable { mutableStateOf("") }
            var message by rememberSaveable { mutableStateOf("Seleziona da 5 a 15 numeri per il tuo set") }

            MagicPage("Inserisci i tuoi numeri", "Seleziona da 5 a 15 numeri ($rangeMin - $rangeMax)", background = R.drawable.bg_number_source) {
                if (selectedNumbers.isNotEmpty()) {
                    NumberGrid(selectedNumbers.sorted(), highlighted = true)
                    Spacer(Modifier.height(14.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Numeri inseriti: ${selectedNumbers.size} / 15", color = MagicMuted, fontSize = 12.sp)
                    Text("Corrente: ${currentInput.ifEmpty { "—" }}", color = MagicGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(18.dp))
                NumberKeypad(
                    onDigit = { digit ->
                        if (currentInput.length < 3 && selectedNumbers.size < 15) {
                            currentInput = (currentInput + digit).trimStart('0').ifEmpty { "0" }
                        }
                    },
                    onBackspace = {
                        if (currentInput.isNotEmpty()) {
                            currentInput = currentInput.dropLast(1)
                        } else if (selectedNumbers.isNotEmpty()) {
                            selectedNumbers = selectedNumbers.dropLast(1)
                        }
                    },
                    onConfirm = {
                        val value = currentInput.toIntOrNull()
                        when {
                            selectedNumbers.size >= 15 -> {
                                message = "Hai già selezionato 15 numeri"
                                currentInput = ""
                            }
                            value == null -> {
                                message = "Digita prima un numero"
                                currentInput = ""
                            }
                            value !in rangeMin..rangeMax -> {
                                message = "Il numero deve essere compreso tra $rangeMin e $rangeMax"
                                currentInput = ""
                            }
                            value in selectedNumbers -> {
                                message = "$value è già presente nel set"
                                currentInput = ""
                            }
                            else -> {
                                selectedNumbers = selectedNumbers + value
                                currentInput = ""
                                message = "Seleziona da 5 a 15 numeri per il tuo set"
                            }
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
                Text(message, color = MagicMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                MagicButton("CONFERMA NUMERI", enabled = selectedNumbers.size in 5..15) {
                    if (selectedNumbers.size in 5..15) {
                        startActivity(Intent(this@ManualNumbersActivity, CombinationConfigActivity::class.java).apply {
                            putIntegerArrayListExtra(EXTRA_SELECTED_NUMBERS, ArrayList(selectedNumbers.distinct().sorted()))
                        })
                    } else {
                        message = "Servono da 5 a 15 numeri per creare combinazioni"
                    }
                }
                if (selectedNumbers.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    MagicButton("AZZERA SET", secondary = true) {
                        selectedNumbers = emptyList()
                        currentInput = ""
                        message = "Set azzerato"
                    }
                }
            }
        }
    }
}

class CombinationConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedNumbers = intent.getIntegerArrayListExtra(EXTRA_SELECTED_NUMBERS)?.distinct()?.sorted() ?: manualNumbers

        magicContent {
            val maxK = selectedNumbers.size.coerceAtLeast(1)
            var combinationSize by rememberSaveable { mutableIntStateOf(minOf(6, maxK)) }
            var luckyCount by rememberSaveable { mutableIntStateOf(1) }
            val total = remember(selectedNumbers, combinationSize) {
                MagicEngine.combinationsCount(selectedNumbers.size, combinationSize)
            }
            val maxLucky = total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt().coerceAtLeast(1)
            if (luckyCount > maxLucky) luckyCount = maxLucky
            if (luckyCount < 1) luckyCount = 1

            MagicPage("Configura la tua magia", "Hai scelto ${selectedNumbers.size} numeri", background = R.drawable.bg_number_source) {
                MagicCard(accent = MagicGold, contentPadding = 16.dp) {
                    NumberGrid(selectedNumbers, highlighted = true)
                }
                Spacer(Modifier.height(24.dp))
                MagicSectionTitle("NUMERI PER COMBINAZIONE")
                Spacer(Modifier.height(10.dp))
                MagicSlider("Numeri per combinazione", combinationSize, 1..maxK) {
                    combinationSize = it.coerceIn(1, maxK)
                }
                Spacer(Modifier.height(8.dp))
                Text(combinationLabel(combinationSize), color = MagicMuted, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                MagicSectionTitle("QUANTE COMBINAZIONI FORTUNATE?")
                Spacer(Modifier.height(10.dp))
                MagicStepper("Combinazioni da generare", luckyCount, luckyCount > 1, luckyCount < maxLucky) {
                    luckyCount = (luckyCount + it).coerceIn(1, maxLucky)
                }
                Spacer(Modifier.height(24.dp))
                MagicCard(accent = MagicGold) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("COMBINAZIONI POSSIBILI", color = MagicMuted, fontSize = 11.sp, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (total == Long.MAX_VALUE) "∞" else total.toString(),
                            color = MagicGold,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "da ${selectedNumbers.size} numeri in ${combinationLabel(combinationSize).lowercase()}",
                            color = MagicMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                MagicButton("PROCEDI ALLA MAGIA") {
                    startActivity(Intent(this@CombinationConfigActivity, LuckyBiometricActivity::class.java).apply {
                        putIntegerArrayListExtra(EXTRA_SELECTED_NUMBERS, ArrayList(selectedNumbers))
                        putExtra(EXTRA_COMBINATION_SIZE, combinationSize)
                        putExtra(EXTRA_LUCKY_COUNT, luckyCount)
                        putExtra(EXTRA_TOTAL_COMBINATIONS, total)
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
            val sessions = remember {
                androidx.compose.runtime.mutableStateListOf(
                    *com.magicnumber.app.domain.magic.SessionHistoryStore.loadAll(this@HistoryActivity).toTypedArray()
                )
            }
            var confirmDeleteAll by remember { mutableStateOf(false) }
            MagicPage(
                "Le mie sessioni",
                "Le tue combinazioni salvate",
                background = R.drawable.bg_number_source,
                bottomBar = {
                    MagicBottomNav(
                        selected = "storico",
                        onHome = { open<HomeActivity>() },
                        onMagic = { open<NumberSourceActivity>() },
                        onHistory = {},
                        onMore = { open<SettingsActivity>() }
                    )
                }
            ) {
                if (sessions.isEmpty()) {
                    MagicCard(accent = MagicPurple, contentPadding = 20.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Nessuna sessione salvata", fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text("Completa una generazione e salvala qui.", color = MagicMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    MagicButton("Elimina tutto", secondary = true, modifier = Modifier.fillMaxWidth()) {
                        confirmDeleteAll = true
                    }
                    Spacer(Modifier.height(14.dp))
                    sessions.forEach { session ->
                        key(session.timestampMillis) {
                            SessionHistoryCard(
                                session = session,
                                onDelete = {
                                    com.magicnumber.app.domain.magic.SessionHistoryStore.delete(this@HistoryActivity, session.timestampMillis)
                                    sessions.remove(session)
                                }
                            )
                            Spacer(Modifier.height(14.dp))
                        }
                    }
                }
            }
            if (confirmDeleteAll) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { confirmDeleteAll = false },
                    title = { Text("Eliminare tutte le sessioni?") },
                    text = { Text("Questa azione non può essere annullata.") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            com.magicnumber.app.domain.magic.SessionHistoryStore.deleteAll(this@HistoryActivity)
                            sessions.clear()
                            confirmDeleteAll = false
                        }) { Text("Elimina tutto", color = MagicPink) }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { confirmDeleteAll = false }) { Text("Annulla") }
                    }
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(session: com.magicnumber.app.domain.magic.SavedSession, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val formatted = remember(session.timestampMillis) {
        java.time.Instant.ofEpochMilli(session.timestampMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", java.util.Locale.ITALIAN))
    }
    val badge = "${session.combinations.size} ${combinationLabelPlural(session.combinationSize)}"
    MagicCard(modifier = Modifier.animateContentSize(), onClick = { expanded = !expanded }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(formatted, color = MagicMuted, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MagicChip(badge, selected = true) {}
                MagicGlowIcon("✕", accent = MagicPink, size = 30.dp) { confirmDelete = true }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (!expanded) {
            val preview = session.combinations.firstOrNull()?.joinToString(" ") { it.toString().padStart(2, '0') } ?: ""
            Text(
                if (session.combinations.size > 1) "$preview …" else preview,
                color = MagicGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(6.dp))
            Text("Tocca per vedere tutte", color = MagicMuted, fontSize = 11.sp)
        } else {
            session.combinations.forEachIndexed { i, combination ->
                Text("Combinazione ${i + 1}", color = MagicMuted, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    combination.forEach { number ->
                        NumberBall(number, size = 38.dp)
                    }
                }
                if (i < session.combinations.lastIndex) Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text("Tocca per richiudere", color = MagicMuted, fontSize = 11.sp)
        }
    }
    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminare questa sessione?") },
            text = { Text("Questa azione non può essere annullata.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onDelete()
                    confirmDelete = false
                }) { Text("Elimina", color = MagicPink) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) { Text("Annulla") }
            }
        )
    }
}

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        magicContent {
            MagicPage(
                "Impostazioni",
                "Personalizza la tua esperienza",
                background = R.drawable.bg_number_source,
                bottomBar = {
                    MagicBottomNav(
                        selected = "altro",
                        onHome = { open<HomeActivity>() },
                        onMagic = { open<NumberSourceActivity>() },
                        onHistory = { open<HistoryActivity>() },
                        onMore = {}
                    )
                }
            ) {
                var rangeMin by remember { mutableIntStateOf(NumberRangePreferences.getMin(this@SettingsActivity)) }
                var rangeMax by remember { mutableIntStateOf(NumberRangePreferences.getMax(this@SettingsActivity)) }
                var soundEnabled by remember { mutableStateOf(SettingsPreferences.isSoundEnabled(this@SettingsActivity)) }
                var vibrationEnabled by remember { mutableStateOf(SettingsPreferences.isVibrationEnabled(this@SettingsActivity)) }
                var effectsEnabled by remember { mutableStateOf(SettingsPreferences.isEffectsEnabled(this@SettingsActivity)) }
                var animationsEnabled by remember { mutableStateOf(SettingsPreferences.isAnimationsEnabled(this@SettingsActivity)) }
                var showInfo by remember { mutableStateOf(false) }
                var showPrivacy by remember { mutableStateOf(false) }
                var showResetConfirm by remember { mutableStateOf(false) }

                MagicSectionTitle("INTERVALLO NUMERI PREDEFINITO")
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MagicStepper("Da", rangeMin, rangeMin > 0, rangeMin < rangeMax, modifier = Modifier.weight(1f), compact = true) {
                        rangeMin = (rangeMin + it).coerceIn(0, rangeMax)
                        NumberRangePreferences.setRange(this@SettingsActivity, rangeMin, rangeMax)
                    }
                    MagicStepper("A", rangeMax, rangeMax > rangeMin, rangeMax < 999, modifier = Modifier.weight(1f), compact = true) {
                        rangeMax = (rangeMax + it).coerceIn(rangeMin, 999)
                        NumberRangePreferences.setRange(this@SettingsActivity, rangeMin, rangeMax)
                    }
                }
                Spacer(Modifier.height(20.dp))
                MagicCard(contentPadding = 6.dp) {
                    SettingsRow("Suoni", "🔊", soundEnabled) {
                        soundEnabled = it
                        SettingsPreferences.setSoundEnabled(this@SettingsActivity, it)
                    }
                    MagicRowDivider()
                    SettingsRow("Vibrazione", "📳", vibrationEnabled) {
                        vibrationEnabled = it
                        SettingsPreferences.setVibrationEnabled(this@SettingsActivity, it)
                        if (it) MagicFeedback.tap(this@SettingsActivity)
                    }
                    MagicRowDivider()
                    SettingsRow("Effetti grafici", "✦", effectsEnabled) {
                        effectsEnabled = it
                        SettingsPreferences.setEffectsEnabled(this@SettingsActivity, it)
                    }
                    MagicRowDivider()
                    SettingsRow("Animazioni", "◎", animationsEnabled) {
                        animationsEnabled = it
                        SettingsPreferences.setAnimationsEnabled(this@SettingsActivity, it)
                    }
                    MagicRowDivider()
                    MagicChevronRow("Tema", "◑", value = "Scuro")
                }
                Spacer(Modifier.height(20.dp))
                MagicCard(contentPadding = 6.dp) {
                    MagicChevronRow("Informazioni", "ℹ") { showInfo = true }
                    MagicRowDivider()
                    MagicChevronRow("Privacy e sicurezza", "🔒") { showPrivacy = true }
                    MagicRowDivider()
                    MagicChevronRow("Ripristina dati", "↺") { showResetConfirm = true }
                }

                if (showInfo) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showInfo = false },
                        title = { Text("Number Magic") },
                        text = { Text("Genera e gestisci le tue combinazioni numeriche fortunate. Tutti i dati restano solo su questo dispositivo, nessuna connessione di rete è richiesta.") },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = { showInfo = false }) { Text("Chiudi") }
                        }
                    )
                }
                if (showPrivacy) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showPrivacy = false },
                        title = { Text("Privacy e sicurezza") },
                        text = { Text("Number Magic funziona interamente offline: numeri, sessioni salvate e preferenze sono memorizzati solo sul tuo dispositivo e non vengono mai inviati altrove.") },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = { showPrivacy = false }) { Text("Chiudi") }
                        }
                    )
                }
                if (showResetConfirm) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showResetConfirm = false },
                        title = { Text("Ripristinare tutti i dati?") },
                        text = { Text("Verranno cancellate tutte le sessioni salvate e le impostazioni torneranno ai valori predefiniti. Questa azione non può essere annullata.") },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                com.magicnumber.app.domain.magic.SessionHistoryStore.deleteAll(this@SettingsActivity)
                                SettingsPreferences.resetAll(this@SettingsActivity)
                                showResetConfirm = false
                                this@SettingsActivity.recreate()
                            }) { Text("Ripristina", color = MagicPink) }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showResetConfirm = false }) { Text("Annulla") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, symbol: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MagicGlowIcon(symbol, accent = MagicPurple, size = 40.dp)
            Text(label, fontWeight = FontWeight.Bold, color = Color.White)
        }
        MagicSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MagicSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor by animateFloatAsState(if (checked) 1f else 0f, label = "switch-track")
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val thumbScale by animateFloatAsState(if (pressed) .9f else 1f, label = "switch-thumb-scale")
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(RoundedCornerShape(50))
            .background(lerp(MagicSurface, MagicGold.copy(alpha = .9f), trackColor))
            .border(1.dp, if (checked) MagicGold else MagicPurple.copy(alpha = .5f), RoundedCornerShape(50))
            .clickable(interactionSource = interactionSource, indication = null) { onCheckedChange(!checked) }
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .graphicsLayer { scaleX = thumbScale; scaleY = thumbScale }
                .clip(CircleShape)
                .background(if (checked) Color(0xFF241600) else Color.White)
        )
    }
}

@Composable
private fun NumberGrid(numbers: List<Int>, highlighted: Boolean = false, animate: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        numbers.chunked(5).forEachIndexed { rowIndex, rowNumbers ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                rowNumbers.forEachIndexed { colIndex, number ->
                    NumberBall(number, highlighted, index = rowIndex * 5 + colIndex, animate = animate)
                }
            }
        }
    }
}

@Composable
private fun NumberKeypad(onDigit: (String) -> Unit, onBackspace: () -> Unit, onConfirm: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("←", "0", "✓")
    )
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        rows.forEach { keys ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                keys.forEach { key ->
                    KeypadKey(key, Modifier.weight(1f)) {
                        when (key) {
                            "←" -> onBackspace()
                            "✓" -> onConfirm()
                            else -> onDigit(key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(key: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .92f else 1f, label = "key-scale")
    Box(
        modifier = modifier
            .height(58.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(16.dp))
            .background(if (key == "✓") MagicGold.copy(alpha = .22f) else MagicSurface)
            .border(1.dp, if (key == "✓") MagicGold else MagicPurple.copy(alpha = .25f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) {
                if (key == "✓") MagicFeedback.confirm(context) else MagicFeedback.tap(context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(key, color = if (key == "✓") MagicGold else Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
    }
}

