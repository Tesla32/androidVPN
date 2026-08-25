package app.cloavy.vpn.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.presentation.components.CyberCard
import app.cloavy.vpn.presentation.components.NeonButton
import app.cloavy.vpn.presentation.components.ScreenTitle
import app.cloavy.vpn.presentation.theme.CyberColors

@Composable
fun OnboardingScreenV7(lang: AppLang, onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }

    val slides = if (lang == AppLang.RU) {
        listOf(
            Triple(
                "🛡",
                "Защита в один тап",
                "Cloavy сам создает персональный VPN-доступ и подключает защиту без QR-кодов и файлов."
            ),
            Triple(
                "⚡",
                "Сценарии под задачу",
                "Кафе, банк, работа, поездки, скорость и приватность — выбирайте режим под ситуацию."
            ),
            Triple(
                "🔑",
                "Тестовый доступ",
                "Для закрытого теста доступ выдается автоматически. После проверки подключим оплату и личные права."
            )
        )
    } else {
        listOf(
            Triple(
                "🛡",
                "One-tap protection",
                "Cloavy creates personal VPN access and connects protection without QR codes or config files."
            ),
            Triple(
                "⚡",
                "Modes for your task",
                "Cafe, bank, work, travel, speed and privacy — choose the mode for the situation."
            ),
            Triple(
                "🔑",
                "Test access",
                "For closed testing, access is issued automatically. Payments and account rights will come next."
            )
        )
    }

    val slide = slides[page]

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            ScreenTitle(
                if (lang == AppLang.RU) "Добро пожаловать" else "Welcome",
                if (lang == AppLang.RU) "Быстрый старт Cloavy VPN" else "Quick start with Cloavy VPN"
            )
        }

        item {
            CyberCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(slide.first, fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        slide.second,
                        color = CyberColors.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))

                    Text(
                        slide.third,
                        color = CyberColors.TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(22.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        slides.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (index == page) 26.dp else 8.dp, 8.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(
                                        if (index == page) CyberColors.SecondaryNeon
                                        else CyberColors.Border
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    NeonButton(
                        text = if (page == slides.lastIndex) {
                            if (lang == AppLang.RU) "НАЧАТЬ" else "START"
                        } else {
                            if (lang == AppLang.RU) "ДАЛЕЕ" else "NEXT"
                        },
                        enabled = true,
                        onClick = {
                            if (page < slides.lastIndex) page += 1 else onFinish()
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (lang == AppLang.RU) "Пропустить" else "Skip",
                        color = CyberColors.TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { onFinish() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}