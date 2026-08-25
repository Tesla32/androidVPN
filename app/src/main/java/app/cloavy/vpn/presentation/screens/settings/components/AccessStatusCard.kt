package app.cloavy.vpn.presentation.screens.settings.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.components.CyberCard
import app.cloavy.vpn.presentation.components.FeatureLine
import app.cloavy.vpn.presentation.theme.CyberColors
import app.cloavy.vpn.utils.TextUtils

@Composable
fun AccessStatusCard(lang: AppLang, accessExpiresAt: String) {
    CyberCard {
        Text(
            if (lang == AppLang.RU) "Тестовый доступ" else "Test access",
            color = CyberColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        FeatureLine("🔑", TextUtils.accessLabel(accessExpiresAt, lang))
        FeatureLine(
            "🧪",
            if (lang == AppLang.RU) "Режим закрытого тестирования" else "Closed test mode"
        )
        FeatureLine(
            "🛒",
            if (lang == AppLang.RU) "Подписки добавим на следующем этапе"
            else "Subscriptions will be added at the next stage"
        )
    }
}