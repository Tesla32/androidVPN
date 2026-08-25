package app.cloavy.vpn.presentation.screens.modes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.components.ScreenTitle
import app.cloavy.vpn.presentation.components.SelectableCyberRow
import app.cloavy.vpn.utils.Constants

@Composable
fun ModesScreenV4(
    lang: AppLang,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenTitle(
                if (lang == AppLang.RU) "Режимы защиты" else "Protection modes",
                if (lang == AppLang.RU) "Выберите сценарий под свою ситуацию"
                else "Choose a scenario for your situation"
            )
        }

        items(Constants.PROTECTION_MODES.indices.toList()) { index ->
            val item = Constants.PROTECTION_MODES[index]
            SelectableCyberRow(
                icon = item.icon,
                title = item.getName(lang),
                desc = item.getDesc(lang),
                selected = selected == index,
                enabled = true,
                trailing = if (selected == index) "✓" else "○",
                onClick = { onSelect(index) }
            )
        }
    }
}