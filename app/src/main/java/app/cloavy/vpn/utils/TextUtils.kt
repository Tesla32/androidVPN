package app.cloavy.vpn.utils

import app.cloavy.vpn.data.models.AppLang
import java.text.SimpleDateFormat
import java.util.*

object TextUtils {

    fun cleanStatus(status: String, lang: AppLang): String {
        if (status.isBlank()) return if (lang == AppLang.RU) "Готово" else "Ready"

        val cleaned = status
            .replace("WireGuard", "Cloavy")
            .replace("backend", if (lang == AppLang.RU) "сервер" else "server")
            .replace("config", if (lang == AppLang.RU) "доступ" else "access")

        if (lang == AppLang.EN) {
            val lower = cleaned.lowercase()
            return when {
                lower.contains("доступ готов") -> "Access ready. Tap Activate"
                lower.contains("нажмите активировать") -> "Tap Activate"
                lower.contains("доступ сброшен") -> "Access reset. Tap Activate"
                lower.contains("получаем персональный доступ") -> "Creating personal access"
                lower.contains("сервер временно недоступен") -> "Server is temporarily unavailable"
                lower.contains("подтвердите vpn") -> "Confirm Android VPN permission"
                lower.contains("разрешение vpn не выдано") -> "VPN permission was not granted"
                lower.contains("нет доступа") -> "No access yet. Tap Activate"
                lower.contains("подключаем защиту") -> "Connecting protection"
                lower.contains("защита активна") -> "Protection active"
                lower.contains("отключаем защиту") -> "Disconnecting protection"
                lower.contains("защита выключена") -> "Protection disabled"
                lower.contains("не удалось подключиться") -> "Could not connect. Try again"
                lower.contains("не удалось отключить") -> "Could not disconnect. Try again"
                lower.contains("готово к защите") -> "Ready to protect"
                else -> cleaned
            }.take(96)
        }

        return cleaned.take(96)
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    fun accessLabel(expiresAt: String, lang: AppLang): String {
        if (expiresAt.isBlank()) {
            return if (lang == AppLang.RU) "Будет выдан автоматически"
            else "Will be issued automatically"
        }

        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = runCatching { input.parse(expiresAt) }.getOrNull()
            ?: return if (lang == AppLang.RU) "Доступ активен" else "Access active"

        val output = if (lang == AppLang.RU) {
            SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        } else {
            SimpleDateFormat("MMM d, yyyy", Locale.US)
        }

        return if (lang == AppLang.RU) "Активен до ${output.format(date)}"
        else "Active until ${output.format(date)}"
    }

    fun serverPingText(value: String, lang: AppLang): String {
        return if (lang == AppLang.EN && value == "скоро") "soon" else value
    }

    fun defaultAccessExpiresAtIso(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(Date(System.currentTimeMillis() + 7L * 24L * 60L * 60L * 1000L))
    }
}