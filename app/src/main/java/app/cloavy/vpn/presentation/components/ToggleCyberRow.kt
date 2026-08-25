package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun ToggleCyberRow(
    icon: String,
    title: String,
    desc: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    CyberCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                icon,
                fontSize = 22.sp,
                modifier = Modifier.width(34.dp),
                textAlign = TextAlign.Center
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = CyberColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(desc, color = CyberColors.TextMuted, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberColors.TextPrimary,
                    checkedTrackColor = CyberColors.PrimaryNeon
                )
            )
        }
    }
}