package app.cloavy.vpn.presentation.screens.status

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.data.models.ProtectionMode
import app.cloavy.vpn.data.models.VpnServer
import app.cloavy.vpn.presentation.components.CyberCard
import app.cloavy.vpn.presentation.components.ScreenTitle
import app.cloavy.vpn.presentation.components.StatBox
import app.cloavy.vpn.presentation.screens.home.components.InfoStackCard
import app.cloavy.vpn.presentation.theme.CyberColors
import app.cloavy.vpn.utils.TextUtils

@Composable
fun StatusScreenV4(
    lang: AppLang,
    connected: Boolean,
    busy: Boolean,
    statusText: String,
    mode: ProtectionMode,
    server: VpnServer,
    sessionSeconds: Long,
    clientName: String,
    accessExpiresAt: String
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            ScreenTitle(
                if (lang == AppLang.RU) "Статус" else "Status",
                if (lang == AppLang.RU) "Детали текущей сессии" else "Current session details"
            )
        }

        item {
            CyberCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                (if (connected) CyberColors.Success else CyberColors.PrimaryNeon)
                                    .copy(alpha = 0.18f),
                                radius = size.minDimension * 0.48f
                            )
                            drawArc(
                                if (connected) CyberColors.Success else CyberColors.PrimaryNeon,
                                -90f,
                                if (connected) 360f else 180f,
                                false,
                                style = Stroke(width = 7f, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            if (connected) "✓" else if (busy) "…" else "!",
                            color = CyberColors.TextPrimary,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (connected) {
                                if (lang == AppLang.RU) "Защита включена" else "Protection enabled"
                            } else {
                                if (lang == AppLang.RU) "Защита выключена" else "Protection disabled"
                            },
                            color = CyberColors.TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            TextUtils.cleanStatus(statusText, lang),
                            color = CyberColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            InfoStackCard(
                rows = listOf(
                    Triple(
                        "⏱",
                        if (lang == AppLang.RU) "Время" else "Time",
                        TextUtils.formatDuration(sessionSeconds)
                    ),
                    Triple(
                        mode.icon,
                        if (lang == AppLang.RU) "Режим" else "Mode",
                        mode.getName(lang)
                    ),
                    Triple(
                        server.flag,
                        if (lang == AppLang.RU) "Сервер" else "Server",
                        server.getName(lang)
                    ),
                    Triple(
                        "🔑",
                        if (lang == AppLang.RU) "Доступ" else "Access",
                        TextUtils.accessLabel(accessExpiresAt, lang)
                    ),
                    Triple(
                        "🧩",
                        if (lang == AppLang.RU) "Клиент" else "Client",
                        if (clientName.isBlank()) "auto" else clientName.take(22)
                    )
                )
            )
        }

        item {
            CyberCard {
                Text(
                    if (lang == AppLang.RU) "Трафик за сессию" else "Session traffic",
                    color = CyberColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatBox(
                        modifier = Modifier.weight(1f),
                        label = if (lang == AppLang.RU) "Принято" else "Received",
                        value = if (connected) "считаем" else "0 МБ"
                    )
                    StatBox(
                        modifier = Modifier.weight(1f),
                        label = if (lang == AppLang.RU) "Отправлено" else "Sent",
                        value = if (connected) "считаем" else "0 МБ"
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (lang == AppLang.RU)
                        "В v4 трафик пока не считаем точно, чтобы не показывать фейковые цифры."
                    else
                        "In v4 traffic is not calculated precisely yet, so we do not show fake numbers.",
                    color = CyberColors.TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}