package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.data.models.Screen
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun CyberBottomNav(current: Screen, lang: AppLang, onSelect: (Screen) -> Unit) {
    val labels = listOf(
        Screen.Home to Pair("⌂", if (lang == AppLang.RU) "Главная" else "Home"),
        Screen.Modes to Pair("☷", if (lang == AppLang.RU) "Режимы" else "Modes"),
        Screen.Servers to Pair("◎", if (lang == AppLang.RU) "Серверы" else "Servers"),
        Screen.Status to Pair("◉", if (lang == AppLang.RU) "Статус" else "Status"),
        Screen.Settings to Pair("⚙", if (lang == AppLang.RU) "Настройки" else "Settings")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CyberColors.Surface.copy(alpha = 0.94f))
            .border(1.dp, CyberColors.Border, RoundedCornerShape(24.dp))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEach { item ->
            val active = current == item.first
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(item.first) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    item.second.first,
                    color = if (active) CyberColors.SecondaryNeon else CyberColors.TextMuted,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    item.second.second,
                    color = if (active) CyberColors.SecondaryNeon else CyberColors.TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}