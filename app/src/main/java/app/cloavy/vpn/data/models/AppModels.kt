package app.cloavy.vpn.data.models

enum class AppLang {
    RU, EN;

    fun toggle() = if (this == RU) EN else RU
}

enum class Screen { Home, Modes, Servers, Status, Settings }

enum class VpnVisualState { Off, Busy, On, Error }

data class ProtectionMode(
    val icon: String,
    val ru: String,
    val en: String,
    val ruDesc: String,
    val enDesc: String
) {
    fun getName(lang: AppLang) = if (lang == AppLang.RU) ru else en
    fun getDesc(lang: AppLang) = if (lang == AppLang.RU) ruDesc else enDesc
}

data class VpnServer(
    val flag: String,
    val ru: String,
    val en: String,
    val city: String,
    val ping: String,
    val active: Boolean
) {
    fun getName(lang: AppLang) = if (lang == AppLang.RU) ru else en
}