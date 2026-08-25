package app.cloavy.vpn.utils

import app.cloavy.vpn.data.models.ProtectionMode
import app.cloavy.vpn.data.models.VpnServer

object Constants {
    const val PREFS_VPN = "cloavy_vpn_v4"
    const val PREFS_UI = "cloavy_v4_ui"

    val PROTECTION_MODES = listOf(
        ProtectionMode("☕", "Кафе", "Cafe", "Защита в публичных Wi-Fi", "Protection on public Wi-Fi"),
        ProtectionMode("🏦", "Банк", "Bank", "Безопасные платежи и финансы", "Safe payments and finance"),
        ProtectionMode("💼", "Работа", "Work", "Доступ к рабочим сервисам", "Access to work services"),
        ProtectionMode("✈", "Путешествие", "Travel", "Интернет в поездках", "Internet while traveling"),
        ProtectionMode("⚡", "Скорость", "Speed", "Оптимизация соединения", "Connection optimization"),
        ProtectionMode("🛡", "Приватность", "Privacy", "Максимум конфиденциальности", "Maximum privacy")
    )

    val VPN_SERVERS = listOf(
        VpnServer("⚡", "Auto", "Auto", "Best route", "auto", true),
        VpnServer("🇪🇺", "Contabo EU", "Contabo EU", "Europe", "72 ms", true),
        VpnServer("🇩🇪", "Германия", "Germany", "Frankfurt", "скоро", false),
        VpnServer("🇳🇱", "Нидерланды", "Netherlands", "Amsterdam", "скоро", false),
        VpnServer("🇺🇸", "США", "USA", "New York", "скоро", false),
        VpnServer("🇯🇵", "Япония", "Japan", "Tokyo", "скоро", false)
    )
}