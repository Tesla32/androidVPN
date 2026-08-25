package app.cloavy.vpn.presentation.screens.servers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.components.ScreenTitle
import app.cloavy.vpn.presentation.components.SelectableCyberRow
import app.cloavy.vpn.utils.Constants
import app.cloavy.vpn.utils.TextUtils

@Composable
fun ServersScreenV4(
    lang: AppLang,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenTitle(
                if (lang == AppLang.RU) "Серверы" else "Servers",
                if (lang == AppLang.RU) "Сейчас активен один тестовый сервер"
                else "One test server is active now"
            )
        }

        items(Constants.VPN_SERVERS.indices.toList()) { index ->
            val item = Constants.VPN_SERVERS[index]
            SelectableCyberRow(
                icon = item.flag,
                title = item.getName(lang),
                desc = "${item.city} • ${TextUtils.serverPingText(item.ping, lang)}",
                selected = selected == index,
                enabled = item.active,
                trailing = if (item.active) {
                    if (selected == index) "✓" else "›"
                } else "soon",
                onClick = { onSelect(index) }
            )
        }
    }
}