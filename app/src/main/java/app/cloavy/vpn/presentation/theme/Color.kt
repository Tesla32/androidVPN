package app.cloavy.vpn.presentation.theme

import androidx.compose.ui.graphics.Color

data class ThemePalette(
    val id: VisualTheme,
    val darkBg: Color,
    val deepBg: Color,
    val surface: Color,
    val surfaceLight: Color,
    val primaryNeon: Color,
    val secondaryNeon: Color,
    val accentPink: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val emoji: String
)

object ColorPalettes {

    val Cyberpunk = ThemePalette(
        id = VisualTheme.Cyberpunk,
        darkBg = Color(0xFF070A12),
        deepBg = Color(0xFF0A1020),
        surface = Color(0xFF101522),
        surfaceLight = Color(0xFF171E2E),
        primaryNeon = Color(0xFFB43CFF),
        secondaryNeon = Color(0xFF1FB6FF),
        accentPink = Color(0xFFFF35C8),
        success = Color(0xFF20E6A1),
        warning = Color(0xFFFFB020),
        danger = Color(0xFFFF4D5E),
        textPrimary = Color(0xFFF4F7FF),
        textSecondary = Color(0xFFA9B2C7),
        textMuted = Color(0xFF687086),
        border = Color(0xFF273149),
        emoji = "🎮"
    )

    val Minimal = ThemePalette(
        id = VisualTheme.Minimal,
        darkBg = Color(0xFF060A11),
        deepBg = Color(0xFF0D1420),
        surface = Color(0xFF111827),
        surfaceLight = Color(0xFF1C2636),
        primaryNeon = Color(0xFF5EA1FF),
        secondaryNeon = Color(0xFF7DEBFF),
        accentPink = Color(0xFFA6F3FF),
        success = Color(0xFF47E6B1),
        warning = Color(0xFFFFC45A),
        danger = Color(0xFFFF6673),
        textPrimary = Color(0xFFF6FAFF),
        textSecondary = Color(0xFFB7C3D6),
        textMuted = Color(0xFF748197),
        border = Color(0xFF293447),
        emoji = "◌"
    )

    val Steppe = ThemePalette(
        id = VisualTheme.Steppe,
        darkBg = Color(0xFF100B06),
        deepBg = Color(0xFF1B1208),
        surface = Color(0xFF221707),
        surfaceLight = Color(0xFF33240D),
        primaryNeon = Color(0xFFE8B14A),
        secondaryNeon = Color(0xFF49C6BD),
        accentPink = Color(0xFFD4934A),
        success = Color(0xFF63D8A4),
        warning = Color(0xFFE8B14A),
        danger = Color(0xFFFF6B5F),
        textPrimary = Color(0xFFFFF7E6),
        textSecondary = Color(0xFFD7C5A2),
        textMuted = Color(0xFF8D7B5C),
        border = Color(0xFF4D3820),
        emoji = "🐎"
    )

    val Brutal = ThemePalette(
        id = VisualTheme.Brutal,
        darkBg = Color(0xFF050605),
        deepBg = Color(0xFF0B0E0A),
        surface = Color(0xFF0F130E),
        surfaceLight = Color(0xFF1A2018),
        primaryNeon = Color(0xFF83FF19),
        secondaryNeon = Color(0xFF6F55FF),
        accentPink = Color(0xFFB43CFF),
        success = Color(0xFF83FF19),
        warning = Color(0xFFFFD84D),
        danger = Color(0xFFFF334D),
        textPrimary = Color(0xFFF4FFE8),
        textSecondary = Color(0xFFB8C8A6),
        textMuted = Color(0xFF738064),
        border = Color(0xFF283322),
        emoji = "💀"
    )
}