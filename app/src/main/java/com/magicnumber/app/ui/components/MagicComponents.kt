package com.magicnumber.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicnumber.app.ui.theme.MagicGold
import com.magicnumber.app.ui.theme.MagicMuted
import com.magicnumber.app.ui.theme.MagicNight
import com.magicnumber.app.ui.theme.MagicPurple
import com.magicnumber.app.ui.theme.MagicSurface

@Composable
fun MagicPage(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedMagicBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            content()
        }
    }
}

@Composable
fun AnimatedMagicBackdrop(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "magic-bg")
    val pulse = transition.animateFloat(
        initialValue = .45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "star-pulse"
    ).value

    val background = Brush.verticalGradient(
        listOf(MagicNight, Color(0xFF080D1D), Color(0xFF110821))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stars = listOf(
                .08f to .11f, .19f to .26f, .82f to .13f, .68f to .29f,
                .91f to .39f, .12f to .47f, .31f to .58f, .78f to .64f,
                .54f to .76f, .18f to .85f, .87f to .91f, .45f to .18f
            )
            stars.forEachIndexed { index, point ->
                drawCircle(
                    color = if (index % 3 == 0) MagicGold else MagicPurple,
                    radius = if (index % 2 == 0) 2.4f else 1.6f,
                    center = androidx.compose.ui.geometry.Offset(size.width * point.first, size.height * point.second),
                    alpha = pulse * if (index % 2 == 0) .7f else .4f
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MagicPurple.copy(alpha = .13f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * .72f, size.height * .26f),
                    radius = size.minDimension * .65f
                ),
                radius = size.minDimension * .65f,
                center = androidx.compose.ui.geometry.Offset(size.width * .72f, size.height * .26f)
            )
        }
        content()
    }
}

@Composable
fun MagicLogoOrb(modifier: Modifier = Modifier, compact: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "logo-orb")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Restart),
        label = "orb-rotation"
    ).value
    val glow = transition.animateFloat(
        initialValue = .4f,
        targetValue = .9f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "orb-glow"
    ).value
    val size = if (compact) 96.dp else 176.dp

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().rotate(rotation)) {
            val radius = this.size.minDimension * .38f
            drawCircle(
                brush = Brush.sweepGradient(listOf(MagicGold, MagicPurple, Color(0xFF45DFFF), MagicGold)),
                radius = radius,
                style = Stroke(width = if (compact) 5f else 8f),
                alpha = glow
            )
            drawCircle(
                color = MagicGold.copy(alpha = .15f),
                radius = radius * 1.22f,
                style = Stroke(width = 2f)
            )
        }
        Text(
            text = "∞",
            fontSize = if (compact) 52.sp else 92.sp,
            fontWeight = FontWeight.Light,
            color = MagicGold
        )
    }
}

@Composable
fun FeaturedMagicCard(
    title: String,
    description: String,
    action: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF17112D).copy(alpha = .94f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(MagicPurple.copy(alpha = .25f), Color.Transparent, MagicGold.copy(alpha = .10f))
                    )
                )
                .padding(22.dp)
        ) {
            Text("✦  OGGI", color = MagicGold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            Text(title, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(description, color = MagicMuted, fontSize = 14.sp)
            Spacer(Modifier.height(18.dp))
            Text(action, color = MagicGold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MagicButton(text: String, onClick: () -> Unit, secondary: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) MagicSurface else MagicGold,
            contentColor = if (secondary) Color.White else Color(0xFF201500)
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun MagicActionCard(title: String, description: String, symbol: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MagicSurface.copy(alpha = .94f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MagicPurple.copy(alpha = .2f))
                    .border(1.dp, MagicPurple.copy(alpha = .65f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(symbol, fontSize = 22.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text(description, color = MagicMuted, fontSize = 13.sp)
            }
            Text("›", color = MagicGold, fontSize = 26.sp)
        }
    }
}

@Composable
fun NumberBall(number: Int, highlighted: Boolean = false) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(if (highlighted) MagicGold.copy(alpha = .18f) else MagicSurface)
            .border(1.dp, if (highlighted) MagicGold else Color(0xFF566080), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            number.toString().padStart(2, '0'),
            fontWeight = FontWeight.Bold,
            color = if (highlighted) MagicGold else Color.White
        )
    }
}
