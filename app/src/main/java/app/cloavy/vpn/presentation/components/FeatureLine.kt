package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun FeatureLine(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            icon,
            fontSize = 19.sp,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text,
            color = CyberColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            lineHeight = 17.sp
        )
    }
}