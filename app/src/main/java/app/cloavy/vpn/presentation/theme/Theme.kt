package app.cloavy.vpn.presentation.theme

import androidx.compose.runtime.*

enum class VisualTheme {
    Steppe, Brutal, Minimal, Cyberpunk
}

object CyberColors {
    private var palette: ThemePalette by mutableStateOf(ColorPalettes.Minimal)

    fun setTheme(theme: VisualTheme) {
        palette = when (theme) {
            VisualTheme.Steppe -> ColorPalettes.Steppe
            VisualTheme.Brutal -> ColorPalettes.Brutal
            VisualTheme.Minimal -> ColorPalettes.Minimal
            VisualTheme.Cyberpunk -> ColorPalettes.Cyberpunk
        }
    }

    val DarkBg get() = palette.darkBg
    val DeepBg get() = palette.deepBg
    val Surface get() = palette.surface
    val SurfaceLight get() = palette.surfaceLight
    val PrimaryNeon get() = palette.primaryNeon
    val SecondaryNeon get() = palette.secondaryNeon
    val AccentPink get() = palette.accentPink
    val Success get() = palette.success
    val Warning get() = palette.warning
    val Danger get() = palette.danger
    val TextPrimary get() = palette.textPrimary
    val TextSecondary get() = palette.textSecondary
    val TextMuted get() = palette.textMuted
    val Border get() = palette.border
    val ThemeEmoji get() = palette.emoji
}