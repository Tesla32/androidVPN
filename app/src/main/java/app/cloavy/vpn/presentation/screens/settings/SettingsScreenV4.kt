package app.cloavy.vpn.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.components.*
import app.cloavy.vpn.presentation.screens.settings.components.AccessStatusCard
import app.cloavy.vpn.presentation.screens.settings.components.ThemePicker
import app.cloavy.vpn.presentation.theme.VisualTheme

@Composable
fun SettingsScreenV4(
    lang: AppLang,
    autoConnect: Boolean,
    notifications: Boolean,
    onAutoToggle: () -> Unit,
    onNotificationsToggle: () -> Unit,
    onLangToggle: () -> Unit,
    visualTheme: VisualTheme,
    accessExpiresAt: String,
    onThemeSelect: (VisualTheme) -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenTitle(
                if (lang == AppLang.RU) "Настройки" else "Settings",
                if (lang == AppLang.RU) "Гибкая настройка защиты" else "Flexible protection setup"
            )
        }

        item {
            ToggleCyberRow(
                "🚀",
                if (lang == AppLang.RU) "Автоподключение" else "Auto connect",
                if (lang == AppLang.RU) "Подключаться при запуске" else "Connect on app start",
                autoConnect,
                onAutoToggle
            )
        }

        item {
            ToggleCyberRow(
                "🔔",
                if (lang == AppLang.RU) "Уведомления" else "Notifications",
                if (lang == AppLang.RU) "Показывать статус защиты" else "Show protection status",
                notifications,
                onNotificationsToggle
            )
        }

        item {
            SettingsActionRow(
                "🌐",
                if (lang == AppLang.RU) "Язык" else "Language",
                if (lang == AppLang.RU) "Русский" else "English",
                onLangToggle
            )
        }

        item {
            ThemePicker(lang = lang, selected = visualTheme, onSelect = onThemeSelect)
        }

        item {
            AccessStatusCard(lang = lang, accessExpiresAt = accessExpiresAt)
        }

        item {
            SettingsActionRow(
                "🧯",
                if (lang == AppLang.RU) "Сбросить тестовый доступ" else "Reset test access",
                if (lang == AppLang.RU) "Создать новый доступ при следующем запуске"
                else "Create new access on next start",
                onReset
            )
        }

        item {
            SettingsActionRow(
                "🛟",
                if (lang == AppLang.RU) "Поддержка" else "Support",
                if (lang == AppLang.RU) "Скоро" else "Soon",
                {}
            )
        }

        item {
            SettingsActionRow(
                "ℹ",
                if (lang == AppLang.RU) "О приложении" else "About",
                "Cloavy VPN v7",
                {}
            )
        }
    }
}