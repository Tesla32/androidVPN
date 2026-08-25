package app.cloavy.vpn.presentation.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.data.models.VpnVisualState
import app.cloavy.vpn.presentation.components.CyberCard
import app.cloavy.vpn.presentation.components.NeonButton
import app.cloavy.vpn.presentation.theme.CyberColors
import app.cloavy.vpn.utils.TextUtils

@Composable
fun ShieldHeroCard(
    lang: AppLang,
    connected: Boolean,
    busy: Boolean,
    statusText: String,
    onPower: () -> Unit
) {
    val visualState = when {
        busy -> VpnVisualState.Busy
        connected -> VpnVisualState.On
        statusText.lowercase().contains("ошиб") || statusText.lowercase().contains("error") ->
            VpnVisualState.Error
        else -> VpnVisualState.Off
    }

    CyberCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShieldCoreButton(visualState = visualState, onClick = onPower)

            Spacer(Modifier.height(8.dp))

            val title = when (visualState) {
                VpnVisualState.On -> if (lang == AppLang.RU) "Защита активна" else "Protection active"
                VpnVisualState.Busy -> if (lang == AppLang.RU) "Подключение" else "Connecting"
                VpnVisualState.Error -> if (lang == AppLang.RU) "Нужна проверка" else "Check required"
                VpnVisualState.Off -> if (lang == AppLang.RU) "Не защищен" else "Not protected"
            }

            Text(
                title,
                color = CyberColors.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Text(
                TextUtils.cleanStatus(statusText, lang),
                color = CyberColors.TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(18.dp))

            NeonButton(
                text = when (visualState) {
                    VpnVisualState.On -> if (lang == AppLang.RU) "ОТКЛЮЧИТЬ" else "DISCONNECT"
                    VpnVisualState.Busy -> if (lang == AppLang.RU) "ПОДКЛЮЧАЕМ" else "CONNECTING"
                    else -> if (lang == AppLang.RU) "АКТИВИРОВАТЬ" else "ACTIVATE"
                },
                enabled = !busy,
                onClick = onPower
            )
        }
    }
}