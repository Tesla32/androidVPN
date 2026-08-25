package app.cloavy.vpn.presentation.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.components.CyberCard
import app.cloavy.vpn.presentation.theme.CyberColors
import app.cloavy.vpn.presentation.theme.VisualTheme
import app.cloavy.vpn.utils.ThemeUtils

@Composable
fun ThemePicker(
    lang: AppLang,
    selected: VisualTheme,
    onSelect: (VisualTheme) -> Unit
) {
    CyberCard {
        Text(
            if (lang == AppLang.RU) "Тема оформления" else "Visual theme",
            color = CyberColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        VisualTheme.values().forEachIndexed { index, theme ->
            val active = selected == theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (active) CyberColors.SurfaceLight.copy(alpha = 0.90f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(theme) }
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    ThemeUtils.themeIcon(theme),
                    fontSize = 22.sp,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ThemeUtils.themeName(theme, lang),
                        color = CyberColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        ThemeUtils.themeDescription(theme, lang),
                        color = CyberColors.TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    if (active) "✓" else "",
                    color = CyberColors.SecondaryNeon,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            if (index != VisualTheme.values().lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CyberColors.Border.copy(alpha = 0.35f))
                )
            }
        }
    }
}