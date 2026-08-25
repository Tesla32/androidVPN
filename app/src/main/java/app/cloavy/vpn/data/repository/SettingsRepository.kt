package app.cloavy.vpn.data.repository

import android.content.Context
import android.content.SharedPreferences
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.theme.VisualTheme
import app.cloavy.vpn.utils.Constants

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_UI, 0)

    fun getLanguage(): AppLang {
        return if (prefs.getString("lang", "ru") == "en") AppLang.EN else AppLang.RU
    }

    fun setLanguage(lang: AppLang) {
        prefs.edit().putString("lang", if (lang == AppLang.RU) "ru" else "en").apply()
    }

    fun getSelectedMode(): Int = prefs.getInt("mode", 0)

    fun setSelectedMode(index: Int) {
        prefs.edit().putInt("mode", index).apply()
    }

    fun getSelectedServer(): Int = prefs.getInt("server", 0)

    fun setSelectedServer(index: Int) {
        prefs.edit().putInt("server", index).apply()
    }

    fun getTheme(): VisualTheme {
        val ordinal = prefs.getInt("theme", VisualTheme.Minimal.ordinal)
        return VisualTheme.values().getOrElse(ordinal) { VisualTheme.Minimal }
    }

    fun setTheme(theme: VisualTheme) {
        prefs.edit().putInt("theme", theme.ordinal).apply()
    }

    fun isAutoConnectEnabled(): Boolean = prefs.getBoolean("auto_connect", false)

    fun setAutoConnect(enabled: Boolean) {
        prefs.edit().putBoolean("auto_connect", enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean = prefs.getBoolean("notifications", true)

    fun setNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("notifications", enabled).apply()
    }

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean("onboarding_done", false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_done", completed).apply()
    }
}