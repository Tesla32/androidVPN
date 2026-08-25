package app.cloavy.vpn.presentation.screens.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.VpnVisualState
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun ShieldCoreButton(visualState: VpnVisualState, onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "shield")

    val pulse by infinite.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val sweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val activeColor by animateColorAsState(
        targetValue = when (visualState) {
            VpnVisualState.On -> CyberColors.Success
            VpnVisualState.Error -> CyberColors.Danger
            else -> CyberColors.PrimaryNeon
        },
        label = "color"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(238.dp)) {
        Canvas(modifier = Modifier.size(230.dp)) {
            drawCircle(
                activeColor.copy(alpha = 0.10f * pulse),
                radius = size.minDimension * 0.48f
            )
            drawCircle(
                CyberColors.SecondaryNeon.copy(alpha = 0.18f),
                radius = size.minDimension * 0.42f,
                style = Stroke(width = 4f)
            )
            drawArc(
                activeColor.copy(alpha = 0.96f),
                -90f + if (visualState == VpnVisualState.Busy) sweep else 0f,
                260f,
                false,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
            drawArc(
                CyberColors.SecondaryNeon.copy(alpha = 0.75f),
                200f,
                110f,
                false,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .size(152.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            activeColor.copy(alpha = 0.34f),
                            CyberColors.Surface.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(1.dp, activeColor.copy(alpha = 0.85f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (visualState == VpnVisualState.On) "🛡" else "⏻",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = when (visualState) {
                        VpnVisualState.On -> "ONLINE"
                        VpnVisualState.Busy -> "SYNC"
                        VpnVisualState.Error -> "RETRY"
                        VpnVisualState.Off -> "START"
                    },
                    color = CyberColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}