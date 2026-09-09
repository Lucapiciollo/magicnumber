package com.magicnumber.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicnumber.app.domain.magic.MagicFeedback
import com.magicnumber.app.ui.theme.MagicAmber
import com.magicnumber.app.ui.theme.MagicBorderSoft
import com.magicnumber.app.ui.theme.MagicGold
import com.magicnumber.app.ui.theme.MagicGoldBright
import com.magicnumber.app.ui.theme.MagicMuted
import com.magicnumber.app.ui.theme.MagicNight
import com.magicnumber.app.ui.theme.MagicNightDeep
import com.magicnumber.app.ui.theme.MagicPurple
import com.magicnumber.app.ui.theme.MagicRadius
import com.magicnumber.app.ui.theme.MagicSpacing
import com.magicnumber.app.ui.theme.MagicSurface
import com.magicnumber.app.ui.theme.MagicSurfaceElevated
import com.magicnumber.app.ui.theme.MagicText
import kotlin.math.max
import kotlinx.coroutines.delay

/** Backs the "Effetti grafici" Settings toggle — when false, ambient glows are skipped entirely. */
val LocalEffectsEnabled = staticCompositionLocalOf { true }

/** Backs the "Animazioni" Settings toggle — when false, entrance/motion animations are skipped. */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

/** Soft ambient glow bleeding beyond the composable's bounds — the app's signature "premium glow". */
private fun DrawScope.drawAmbientGlow(color: Color, alpha: Float, spread: Float = 1.35f) {
    val radius = (max(size.width, size.height) / 2f) * spread
    drawCircle(
        brush = Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent), radius = radius),
        radius = radius,
        center = Offset(size.width / 2f, size.height / 2f)
    )
}

@Composable
private fun Modifier.magicGlow(color: Color, alpha: Float = .22f, spread: Float = 1.35f): Modifier =
    if (LocalEffectsEnabled.current) this.drawBehind { drawAmbientGlow(color, alpha, spread) } else this

/** Small gold, all-caps eyebrow label used above sections ("INTERVALLO", "STRUTTURA", ...). */
@Composable
fun MagicSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = MagicGold,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp
    )
}

/** Circular back button + optional title, used on every non-root screen. */
@Composable
fun MagicTopBar(onBack: () -> Unit, title: String? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MagicGlowIcon(symbol = "←", accent = MagicPurple, size = 40.dp, onClick = onBack)
        if (title != null) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MagicText)
        }
    }
}

/** A round glowing icon chip — used for nav back-arrows, action-card icons and decorative symbols. */
@Composable
fun MagicGlowIcon(
    symbol: String,
    accent: Color = MagicPurple,
    size: Dp = 48.dp,
    onClick: (() -> Unit)? = null
) {
    val shapeModifier = Modifier
        .size(size)
        .magicGlow(accent, alpha = .28f, spread = 1.6f)
        .clip(CircleShape)
        .background(accent.copy(alpha = .16f))
        .border(1.dp, accent.copy(alpha = .7f), CircleShape)
    Box(
        modifier = if (onClick != null) shapeModifier.clickable(onClick = onClick) else shapeModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = (size.value * .42f).sp, color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MagicPage(
    title: String?,
    subtitle: String? = null,
    background: Int,
    onBack: (() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    MagicImageBackdrop(background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .then(if (bottomBar == null) Modifier.navigationBarsPadding() else Modifier)
                    .padding(horizontal = MagicSpacing.screenH, vertical = MagicSpacing.screenV),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (onBack != null) {
                    MagicTopBar(onBack = onBack)
                    Spacer(Modifier.height(18.dp))
                }
                if (title != null) {
                    Text(
                        text = title,
                        fontSize = 30.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    if (subtitle != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(subtitle, color = MagicMuted, textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(28.dp))
                }
                content()
            }
            if (bottomBar != null) bottomBar()
        }
    }
}

/** Full-bleed illustrated background (real mockup art asset) with a light readability overlay. Shared by every screen. */
@Composable
fun MagicImageBackdrop(background: Int, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(background),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = .10f))
        )
        content()
    }
}

/** Base bordered/glowing surface every card in the app is built from. */
@Composable
fun MagicCard(
    modifier: Modifier = Modifier,
    accent: Color = MagicPurple,
    glowAlpha: Float = .14f,
    contentPadding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(MagicRadius.md)
    var base = modifier
        .fillMaxWidth()
        .magicGlow(accent, alpha = glowAlpha, spread = 1.2f)
        .clip(shape)
        .background(MagicSurface.copy(alpha = .94f))
        .border(1.dp, accent.copy(alpha = .38f), shape)
    if (onClick != null) base = base.clickable(onClick = onClick)
    Column(modifier = base.padding(contentPadding), content = content)
}

@Composable
fun FeaturedMagicCard(
    title: String,
    description: String,
    action: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(MagicRadius.lg)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .magicGlow(MagicGold, alpha = .30f, spread = 1.35f)
            .clip(shape)
            .background(Color(0xFF17112D).copy(alpha = .96f))
            .border(1.4.dp, MagicGold.copy(alpha = .65f), shape)
            .background(
                Brush.linearGradient(
                    listOf(MagicPurple.copy(alpha = .28f), Color.Transparent, MagicGold.copy(alpha = .14f))
                )
            )
            .clickable(onClick = onClick)
            .padding(22.dp)
    ) {
        Text("✦  OGGI", color = MagicGold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(10.dp))
        Text(title, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = MagicText)
        Spacer(Modifier.height(6.dp))
        Text(description, color = MagicMuted, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        Text(action, color = MagicGold, fontWeight = FontWeight.Bold)
    }
}

/** Big single-choice card with a centered icon, title, description and trailing arrow — used for the primary option on "Scelta Origine". */
@Composable
fun MagicChoiceCard(
    symbol: String,
    title: String,
    description: String,
    action: String,
    accent: Color = MagicGold,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(MagicRadius.lg)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .magicGlow(accent, alpha = .28f, spread = 1.3f)
            .clip(shape)
            .background(Color(0xFF17112D).copy(alpha = .96f))
            .border(1.4.dp, accent.copy(alpha = .6f), shape)
            .clickable(onClick = onClick)
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MagicGlowIcon(symbol, accent = accent, size = 56.dp)
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MagicText, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(description, color = MagicMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(action, color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Modifier.composedPressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .96f else 1f, label = "press-scale")
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun MagicPrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(MagicRadius.sm)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .composedPressScale(interactionSource)
            .magicGlow(MagicGold, alpha = if (enabled) .32f else .05f, spread = 1.5f)
            .clip(shape)
            .background(
                if (enabled) Brush.horizontalGradient(listOf(MagicAmber, MagicGold, MagicGoldBright))
                else Brush.horizontalGradient(listOf(MagicSurfaceElevated, MagicSurfaceElevated))
            )
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = .4.sp, color = if (enabled) Color(0xFF241600) else MagicMuted)
    }
}

@Composable
fun MagicSecondaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(MagicRadius.sm)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .composedPressScale(interactionSource)
            .clip(shape)
            .background(MagicSurface)
            .border(1.dp, MagicPurple.copy(alpha = .55f), shape)
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (enabled) Color.White else MagicMuted)
    }
}

/** Back-compat wrapper — old call sites keep working, now backed by the gold-gradient / navy design system. */
@Composable
fun MagicButton(text: String, secondary: Boolean = false, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val context = LocalContext.current
    val feedbackClick: () -> Unit = { MagicFeedback.tap(context); onClick() }
    if (secondary) MagicSecondaryButton(text = text, modifier = modifier, enabled = enabled, onClick = feedbackClick)
    else MagicPrimaryButton(text = text, modifier = modifier, enabled = enabled, onClick = feedbackClick)
}

@Composable
fun MagicActionCard(
    title: String,
    description: String,
    symbol: String,
    accent: Color = MagicPurple,
    onClick: () -> Unit
) {
    MagicCard(accent = accent, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MagicGlowIcon(symbol = symbol, accent = accent, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MagicText)
                Spacer(Modifier.height(4.dp))
                Text(description, color = MagicMuted, fontSize = 13.sp)
            }
            Text("›", color = MagicGold, fontSize = 26.sp)
        }
    }
}

/** Selectable pill chip, e.g. quick-pick values (5 / 10 / 12 / 15 / 20). */
@Composable
fun MagicChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(MagicRadius.pill)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) MagicGold.copy(alpha = .22f) else MagicSurface)
            .border(1.dp, if (selected) MagicGold else MagicBorderSoft, shape)
            .clickable { MagicFeedback.tap(context); onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) MagicGold else MagicMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

/** Compact row inside a [MagicCard] group: icon + label (+ optional subtitle) + trailing chevron. Used on Home's action list. */
@Composable
fun MagicListRow(label: String, symbol: String, subtitle: String? = null, accent: Color = MagicPurple, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MagicGlowIcon(symbol = symbol, accent = accent, size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MagicText)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = MagicMuted)
            }
        }
        Text("›", color = MagicGold, fontSize = 22.sp)
    }
}

/** Thin divider used between [MagicListRow]s inside a grouped card. */
@Composable
fun MagicRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MagicBorderSoft.copy(alpha = .5f))
    )
}

/** Compact square tile (icon + label centered) — used for the Storico / Impostazioni pair on Home. */
@Composable
fun MagicTileCard(label: String, symbol: String, accent: Color = MagicPurple, modifier: Modifier = Modifier, onClick: () -> Unit) {
    MagicCard(modifier = modifier, accent = accent, contentPadding = 16.dp, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            MagicGlowIcon(symbol = symbol, accent = accent, size = 36.dp)
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MagicText)
        }
    }
}

/** Settings-style row: icon + label + trailing value/chevron, no toggle — used for pickers (Tema, Animazioni…) and plain links. */
@Composable
fun MagicChevronRow(label: String, symbol: String, value: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MagicGlowIcon(symbol, accent = MagicPurple, size = 40.dp)
        Text(label, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, color = MagicMuted, fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
        }
        Text("›", color = MagicGold, fontSize = 20.sp)
    }
}

@Composable
fun MagicBottomNav(
    selected: String,
    onHome: () -> Unit,
    onMagic: () -> Unit,
    onHistory: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MagicSurface.copy(alpha = .97f))
            .border(width = 1.dp, color = MagicPurple.copy(alpha = .2f))
            .navigationBarsPadding()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MagicNavItem("⌂", "Home", selected == "home", onHome)
        MagicNavItem("✦", "Magia", selected == "magia", onMagic)
        MagicNavItem("◷", "Storico", selected == "storico", onHistory)
        MagicNavItem("⚙", "Altro", selected == "altro", onMore)
    }
}

@Composable
private fun MagicNavItem(symbol: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(symbol, fontSize = 19.sp, color = if (active) MagicGold else MagicMuted)
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) MagicGold else MagicMuted)
    }
}

/**
 * Numeric sphere — the app's core visual motif. When [animate] is true, the ball scales/fades in
 * with a per-[index] stagger, used the first time a set of numbers is revealed.
 */
@Composable
fun NumberBall(number: Int, highlighted: Boolean = false, index: Int = 0, animate: Boolean = false, size: Dp = 54.dp) {
    val animationsEnabled = LocalAnimationsEnabled.current
    var visible by remember(number) { mutableStateOf(!animate || !animationsEnabled) }
    LaunchedEffect(number, animate, animationsEnabled) {
        if (animate && animationsEnabled) {
            visible = false
            delay(index * 65L)
            visible = true
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else .35f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ball-scale"
    )
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "ball-alpha")

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            .then(if (highlighted) Modifier.magicGlow(MagicGold, alpha = .3f, spread = 1.7f) else Modifier)
            .clip(CircleShape)
            .background(
                if (highlighted) Brush.radialGradient(listOf(MagicGold.copy(alpha = .28f), MagicSurfaceElevated))
                else Brush.radialGradient(listOf(MagicSurfaceElevated, MagicSurface))
            )
            .border(1.4.dp, if (highlighted) MagicGold else MagicBorderSoft, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            number.toString().padStart(2, '0'),
            fontWeight = FontWeight.Black,
            fontSize = (size.value * .32f).sp,
            color = if (highlighted) MagicGoldBright else Color.White
        )
    }
}

/** Compact +/- numeric stepper card shared by the Generate and Configuration screens. */
@Composable
fun MagicStepper(label: String, value: Int, canDecrease: Boolean, canIncrease: Boolean, modifier: Modifier = Modifier, onStep: (Int) -> Unit) {
    MagicCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(label, color = MagicMuted, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(value.toString(), color = MagicGold, fontWeight = FontWeight.Black, fontSize = 28.sp, modifier = Modifier.animateContentSize())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MagicStepButton("−", canDecrease) { onStep(-1) }
                MagicStepButton("+", canIncrease) { onStep(1) }
            }
        }
    }
}

@Composable
private fun MagicStepButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .86f else 1f, label = "step-btn-scale")
    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(if (enabled) MagicGold.copy(alpha = .16f) else MagicSurface)
            .border(1.dp, if (enabled) MagicGold else MagicBorderSoft, CircleShape)
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null) {
                MagicFeedback.tap(context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = if (enabled) MagicGold else MagicMuted, fontWeight = FontWeight.Black, fontSize = 20.sp)
    }
}

@Composable
fun InfoCard(label: String, value: String) {
    MagicCard(accent = MagicPurple, contentPadding = 18.dp) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = MagicMuted)
            Text(value, fontWeight = FontWeight.Bold, color = MagicGold)
        }
    }
}

