package app.cloavy.vpn.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.data.models.ProtectionMode
import app.cloavy.vpn.data.models.VpnServer
import app.cloavy.vpn.presentation.components.CyberCard
import app.cloavy.vpn.presentation.components.FeatureLine
import app.cloavy.vpn.presentation.screens.home.components.InfoStackCard
import app.cloavy.vpn.presentation.screens.home.components.ShieldHeroCard
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun HomeScreenV4(
    lang: AppLang,
    connected: Boolean,
    busy: Boolean,
    statusText: String,
    mode: ProtectionMode,
    server: VpnServer,
    onPower: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            ShieldHeroCard(
                lang = lang,
                connected = connected,
                busy = busy,
                statusText = statusText,
                onPower = onPower
            )
        }

        item {
            InfoStackCard(
                rows = listOf(
                    Triple(
                        "📶",
                        if (lang == AppLang.RU) "Сеть" else "Network",
                        "Public Wi-Fi"
                    ),
                    Triple(
                        "🛡",
                        if (lang == AppLang.RU) "Риск" else "Risk",
                        if (connected) {
                            if (lang == AppLang.RU) "Низкий" else "Low"
                        } else {
                            if (lang == AppLang.RU) "Высокий" else "High"
                        }
                    ),
                    Triple(
                        "🌐",
                        if (lang == AppLang.RU) "Сервер" else "Server",
                        server.getName(lang)
                    ),
                    Triple(
                        mode.icon,
                        if (lang == AppLang.RU) "Режим" else "Mode",
                        mode.getName(lang)
                    )
                )
            )
        }

        item {
            CyberCard {
                Text(
                    if (lang == AppLang.RU) "Что делает Cloavy" else "What Cloavy does",
                    color = CyberColors.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                FeatureLine(
                    "🧬",
                    if (lang == AppLang.RU) "Создает персональный VPN-доступ автоматически"
                    else "Creates personal VPN access automatically"
                )
                FeatureLine(
                    "⚡",
                    if (lang == AppLang.RU) "Подключается одной кнопкой"
                    else "Connects with one button"
                )
                FeatureLine(
                    "🔒",
                    if (lang == AppLang.RU) "Скрывает сложную техническую часть"
                    else "Hides complex technical setup"
                )
            }
        }
    }
}