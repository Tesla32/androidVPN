package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun InfoLine(icon: String, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            icon,
            color = CyberColors.SecondaryNeon,
            fontSize = 22.sp,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = CyberColors.TextMuted, fontSize = 11.sp)
            Text(
                value,
                color = CyberColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text("›", color = CyberColors.TextMuted, fontSize = 22.sp)
    }
}