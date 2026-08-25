package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun SelectableCyberRow(
    icon: String,
    title: String,
    desc: String,
    selected: Boolean,
    enabled: Boolean,
    trailing: String,
    onClick: () -> Unit
) {
    val border = if (selected) CyberColors.PrimaryNeon else CyberColors.Border

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .border(1.dp, border, RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(
            containerColor = CyberColors.Surface.copy(alpha = if (enabled) 0.94f else 0.50f)
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        (if (selected) CyberColors.PrimaryNeon else CyberColors.SurfaceLight)
                            .copy(alpha = 0.65f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp, color = CyberColors.TextPrimary)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = CyberColors.TextPrimary.copy(alpha = if (enabled) 1f else 0.45f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    desc,
                    color = CyberColors.TextSecondary.copy(alpha = if (enabled) 1f else 0.45f),
                    fontSize = 12.sp
                )
            }

            Text(
                trailing,
                color = if (selected) CyberColors.SecondaryNeon else CyberColors.TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}