package app.cloavy.vpn.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun CyberHeader(lang: AppLang, onLangToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(CyberColors.PrimaryNeon, CyberColors.SecondaryNeon)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                CyberColors.ThemeEmoji,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Cloavy VPN",
                color = CyberColors.TextPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                if (lang == AppLang.RU) "Один тап - и ты под защитой"
                else "One tap - and you are protected",
                color = CyberColors.TextSecondary,
                fontSize = 12.sp
            )
        }

        NeonSmallButton(
            text = if (lang == AppLang.RU) "EN" else "RU",
            onClick = onLangToggle
        )
    }
}