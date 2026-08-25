package app.cloavy.vpn.utils

import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.theme.VisualTheme

object ThemeUtils {

    fun themeIcon(theme: VisualTheme): String = when (theme) {
        VisualTheme.Steppe -> "🐎"
        VisualTheme.Brutal -> "💀"
        VisualTheme.Minimal -> "◌"
        VisualTheme.Cyberpunk -> "🎮"
    }

    fun themeName(theme: VisualTheme, lang: AppLang): String = when (theme) {
        VisualTheme.Steppe -> if (lang == AppLang.RU) "Степной" else "Steppe"
        VisualTheme.Brutal -> if (lang == AppLang.RU) "Брутальный" else "Brutal"
        VisualTheme.Minimal -> if (lang == AppLang.RU) "Минимал" else "Minimal"
        VisualTheme.Cyberpunk -> if (lang == AppLang.RU) "Игровой" else "Gaming"
    }

    fun themeDescription(theme: VisualTheme, lang: AppLang): String = when (theme) {
        VisualTheme.Steppe -> if (lang == AppLang.RU) "теплый песок, бирюза и степной оберег"
        else "warm sand, turquoise and steppe shield"
        VisualTheme.Brutal -> if (lang == AppLang.RU) "черный, кислотный и дерзкий"
        else "black, acid and bold"
        VisualTheme.Minimal -> if (lang == AppLang.RU) "чистый premium, спокойная защита"
        else "clean premium, calm protection"
        VisualTheme.Cyberpunk -> if (lang == AppLang.RU) "неон, игровой ритм и драйв"
        else "neon, gaming rhythm and drive"
    }
}