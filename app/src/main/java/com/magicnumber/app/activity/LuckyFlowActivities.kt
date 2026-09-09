package com.magicnumber.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.magicnumber.app.R
import com.magicnumber.app.domain.magic.MagicEngine
import com.magicnumber.app.ui.components.MagicButton
import com.magicnumber.app.ui.components.MagicCard
import com.magicnumber.app.ui.components.MagicGlowIcon
import com.magicnumber.app.ui.components.MagicPage
import com.magicnumber.app.ui.components.NumberBall
import com.magicnumber.app.ui.theme.MagicAmber
import com.magicnumber.app.ui.theme.MagicCyan
import com.magicnumber.app.ui.theme.MagicGold
import com.magicnumber.app.ui.theme.MagicMuted
import com.magicnumber.app.ui.theme.MagicNumberTheme
import com.magicnumber.app.ui.theme.MagicPurple
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private val EXTRA_SELECTED_NUMBERS = MagicIntentKeys.SELECTED_NUMBERS
private val EXTRA_COMBINATION_SIZE = MagicIntentKeys.COMBINATION_SIZE
private val EXTRA_LUCKY_COUNT = MagicIntentKeys.LUCKY_COUNT
private val EXTRA_TOTAL_COMBINATIONS = MagicIntentKeys.TOTAL_COMBINATIONS
private const val EXTRA_LUCKY_COMBINATIONS = "lucky_combinations"
private const val EXTRA_MAGIC_DATE = "magic_date"

private fun ComponentActivity.magicContentV2(content: @Composable () -> Unit) {
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

class LuckyBiometricActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val numbers = intent.getIntegerArrayListExtra(EXTRA_SELECTED_NUMBERS)?.distinct()?.sorted().orEmpty()
        val combinationSize = intent.getIntExtra(EXTRA_COMBINATION_SIZE, minOf(6, numbers.size.coerceAtLeast(1)))
        val luckyCount = intent.getIntExtra(EXTRA_LUCKY_COUNT, 4)
        val total = intent.getLongExtra(EXTRA_TOTAL_COMBINATIONS, 0L)
        val today = LocalDate.now()
        val executor: Executor = mainExecutor

        fun generateAndContinue() {
            val combinations = MagicEngine.luckyCombinations(
                context = this,
                numbers = numbers,
                combinationSize = combinationSize,
                requestedCount = luckyCount,
                date = today
            )
            startActivity(Intent(this, LuckyAnimationActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_LUCKY_COMBINATIONS, ArrayList(combinations.map { it.joinToString(",") }))
                putExtra(EXTRA_MAGIC_DATE, today.toString())
                putExtra(EXTRA_COMBINATION_SIZE, combinationSize)
                putExtra(EXTRA_LUCKY_COUNT, combinations.size)
            })
        }

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    generateAndContinue()
                }
            }
        )

        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Attiva la magia")
            .setSubtitle("Conferma l'estrazione fortunata di oggi")
            .setAllowedAuthenticators(authenticators)
            .setNegativeButtonText("Annulla")
            .build()

        val biometricAvailable = BiometricManager.from(this)
            .canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS

        magicContentV2 {
            var status by remember { mutableStateOf("Tocca l'impronta per continuare") }

            fun start() {
                if (biometricAvailable) {
                    status = "Autenticazione in corso…"
                    biometricPrompt.authenticate(promptInfo)
                } else {
                    status = "Biometria non disponibile: generazione locale"
                    generateAndContinue()
                }
            }

            MagicPage("Attiva la magia", "Appoggia il dito per confermare e generare le combinazioni di oggi", background = R.drawable.bg_number_source) {
                BiometricGlyph(onClick = ::start)
                Spacer(Modifier.height(10.dp))
                Text(status, color = MagicMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                MagicCard(accent = MagicPurple) {
                    TrustRow("🔒", "Sicuro", "I tuoi dati restano sul dispositivo")
                    Spacer(Modifier.height(16.dp))
                    TrustRow("👆", "Unico", "Ogni risultato è personale")
                    Spacer(Modifier.height(16.dp))
                    TrustRow("⚡", "Veloce", "Inizia subito la generazione")
                }
                Spacer(Modifier.height(28.dp))
                if (!biometricAvailable) {
                    MagicButton("GENERA SENZA BIOMETRIA") { start() }
                }
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
            var step by remember { mutableIntStateOf(0) }
            var revealReady by remember { mutableStateOf(false) }
            val animationsEnabled = com.magicnumber.app.ui.components.LocalAnimationsEnabled.current
            val comboSize = combinations.firstOrNull()?.size ?: 6
            var decoyNumbers by remember { mutableStateOf(randomDecoyNumbers(comboSize)) }

            LaunchedEffect(animationsEnabled) {
                if (animationsEnabled) {
                    delay(450); step = 1
                    delay(600); step = 2
                    delay(650); step = 3
                    delay(700); step = 4
                    delay(450); revealReady = true
                } else {
                    step = 4
                    revealReady = true
                }
                com.magicnumber.app.domain.magic.MagicFeedback.reveal(this@LuckyAnimationActivity)
            }

            LaunchedEffect(Unit) {
                while (!revealReady) {
                    delay(380)
                    decoyNumbers = randomDecoyNumbers(comboSize)
                    com.magicnumber.app.domain.magic.MagicFeedback.generateTick(this@LuckyAnimationActivity)
                }
            }

            MagicPage("La magia è in corso…", "Il tuo universo numerico sta prendendo forma", background = R.drawable.bg_number_source) {
                OrbitingNumbers(decoyNumbers)
                Spacer(Modifier.height(24.dp))
                ProgressLineV2(if (step >= 1) "✓" else "○", "Analisi dei numeri")
                ProgressLineV2(if (step >= 2) "✓" else "○", "Calcolo combinazioni")
                ProgressLineV2(if (step >= 3) "✓" else "○", "Creazione set magico")
                ProgressLineV2(if (step >= 4) "✓" else "○", "Selezione fortunata")
                Spacer(Modifier.height(28.dp))
                if (revealReady) {
                    MagicButton("RIVELA LE COMBINAZIONI") {
                        startActivity(Intent(this@LuckyAnimationActivity, LuckyResultsActivity::class.java).apply {
                            putStringArrayListExtra(EXTRA_LUCKY_COMBINATIONS, intent.getStringArrayListExtra(EXTRA_LUCKY_COMBINATIONS))
                            putExtra(EXTRA_MAGIC_DATE, date)
                        })
                    }
                } else {
                    Text("Quasi pronto…", color = MagicMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
                date.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString() },
                background = R.drawable.bg_number_source
            ) {
                combinations.forEachIndexed { index, combination ->
                    LuckyResultCard(index + 1, combination)
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    var saved by remember { mutableStateOf(false) }
                    MagicButton(if (saved) "SALVATA ✓" else "SALVA", secondary = true, modifier = Modifier.weight(1f)) {
                        if (!saved) {
                            com.magicnumber.app.domain.magic.SessionHistoryStore.save(this@LuckyResultsActivity, combinations.firstOrNull()?.size ?: 0, combinations)
                            saved = true
                        }
                    }
                    MagicButton("CONDIVIDI", secondary = true, modifier = Modifier.weight(1f)) {
                        val text = combinations.joinToString("\n") { c -> c.joinToString("  ·  ") { n -> n.toString().padStart(2, '0') } }
                        startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Le mie combinazioni fortunate di oggi:\n$text")
                                },
                                null
                            )
                        )
                    }
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
                Spacer(Modifier.height(10.dp))
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

private fun readCombinations(intent: Intent): List<List<Int>> =
    intent.getStringArrayListExtra(EXTRA_LUCKY_COMBINATIONS).orEmpty().mapNotNull { encoded ->
        encoded.split(',').mapNotNull(String::toIntOrNull).takeIf { it.isNotEmpty() }
    }

/** Decoy numbers shown while the magic is "in progress" — never the real result, so the reveal stays a surprise. */
private fun randomDecoyNumbers(size: Int): List<Int> = (1..90).shuffled().take(size.coerceAtLeast(1))

@Composable
private fun MagicSummaryCard(label: String, value: String) {
    MagicCard(accent = MagicPurple, contentPadding = 17.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

/** Visible fingerprint button that triggers biometric auth. */
@Composable
private fun BiometricGlyph(onClick: () -> Unit = {}) {
    MagicGlowIcon("🫆", accent = MagicGold, size = 148.dp, onClick = onClick)
}

/** Simple icon + title + description row used on the Autenticazione screen's trust list. */
@Composable
private fun TrustRow(symbol: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MagicGlowIcon(symbol, accent = MagicPurple, size = 44.dp)
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = MagicGold, fontSize = 14.sp)
            Text(description, color = MagicMuted, fontSize = 12.sp)
        }
    }
}

/** Numbers orbiting a glowing core — the "energia numerica" motif on the processing screen. */
@Composable
private fun OrbitingNumbers(numbers: List<Int>) {
    val animationsEnabled = com.magicnumber.app.ui.components.LocalAnimationsEnabled.current
    val transition = rememberInfiniteTransition(label = "orbit")
    val animatedAngle = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "orbit-angle"
    ).value
    val angle = if (animationsEnabled) animatedAngle else 0f

    Box(
        modifier = Modifier
            .size(224.dp)
            .background(Color.Black.copy(alpha = .40f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        MagicGlowIcon("✦", accent = MagicGold, size = 68.dp)
        val shown = numbers.take(8).ifEmpty { listOf(0) }
        shown.forEachIndexed { index, number ->
            val baseAngle = (360f / shown.size) * index
            val radians = Math.toRadians((angle + baseAngle).toDouble())
            val radius = 96f
            val x = (radius * cos(radians)).toFloat()
            val y = (radius * sin(radians)).toFloat()
            Box(modifier = Modifier.offset(x = x.dp, y = y.dp)) {
                NumberBall(number, highlighted = true, size = 38.dp)
            }
        }
    }
}

@Composable
private fun LuckyResultCard(rank: Int, numbers: List<Int>) {
    val accent = listOf(MagicGold, MagicPurple, MagicCyan, MagicAmber)[(rank - 1) % 4]
    MagicCard(accent = accent, contentPadding = 12.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MagicGlowIcon(rank.toString(), accent = accent, size = 34.dp)
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                numbers.forEach { NumberBall(it, highlighted = true, size = 34.dp) }
            }
        }
    }
}

