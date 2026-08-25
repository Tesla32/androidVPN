package app.cloavy.vpn.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cloavy.vpn.presentation.components.CyberCard
import app.cloavy.vpn.presentation.components.InfoLine
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun InfoStackCard(rows: List<Triple<String, String, String>>) {
    CyberCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEachIndexed { index, row ->
                InfoLine(icon = row.first, title = row.second, value = row.third)
                if (index != rows.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CyberColors.Border.copy(alpha = 0.55f))
                    )
                }
            }
        }
    }
}