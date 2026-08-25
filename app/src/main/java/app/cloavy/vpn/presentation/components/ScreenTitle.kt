package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun ScreenTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
        Text(
            title,
            color = CyberColors.TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            subtitle,
            color = CyberColors.TextSecondary,
            fontSize = 13.sp
        )
    }
}