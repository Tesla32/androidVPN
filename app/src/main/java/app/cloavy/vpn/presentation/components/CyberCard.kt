package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun CyberCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberColors.Border, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = CyberColors.Surface.copy(alpha = 0.92f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            CyberColors.Surface.copy(alpha = 0.96f),
                            CyberColors.SurfaceLight.copy(alpha = 0.55f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            content()
        }
    }
}