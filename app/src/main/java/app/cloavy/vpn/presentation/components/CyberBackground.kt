package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun CyberBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = CyberColors.PrimaryNeon.copy(alpha = 0.14f),
            radius = size.minDimension * 0.45f,
            center = Offset(size.width * 0.88f, size.height * 0.12f)
        )
        drawCircle(
            color = CyberColors.SecondaryNeon.copy(alpha = 0.10f),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.10f, size.height * 0.78f)
        )
        for (i in 0..8) {
            val x = size.width * (0.05f + i * 0.13f)
            drawLine(
                color = CyberColors.PrimaryNeon.copy(alpha = 0.06f),
                start = Offset(x, 0f),
                end = Offset(x + 120f, size.height),
                strokeWidth = 2f
            )
        }
    }
}