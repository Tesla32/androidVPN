package app.cloavy.vpn.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.cloavy.vpn.data.models.AppLang
import app.cloavy.vpn.data.models.Screen
import app.cloavy.vpn.data.repository.SettingsRepository
import app.cloavy.vpn.data.repository.VpnRepository
import app.cloavy.vpn.presentation.components.*
import app.cloavy.vpn.presentation.screens.home.HomeScreenV4
import app.cloavy.vpn.presentation.screens.modes.ModesScreenV4
import app.cloavy.vpn.presentation.screens.servers.ServersScreenV4
import app.cloavy.vpn.presentation.screens.settings.SettingsScreenV4
import app.cloavy.vpn.presentation.screens.status.StatusScreenV4
import app.cloavy.vpn.presentation.screens.onboarding.OnboardingScreenV7
import app.cloavy.vpn.presentation.theme.CyberColors
import app.cloavy.vpn.utils.Constants
import kotlinx.coroutines.delay

@Composable
fun CloavyApp(
    activity: MainActivity,
    vpnRepository: VpnRepository,
    settingsRepository: SettingsRepository
) {
    var lang by remember { mutableStateOf(settingsRepository.getLanguage()) }
    var screen by remember { mutableStateOf(Screen.Home) }
    var selectedMode by remember { mutableIntStateOf(settingsRepository.getSelectedMode()) }
    var selectedServer by remember { mutableIntStateOf(settingsRepository.getSelectedServer()) }
    var visualTheme by remember { mutableStateOf(settingsRepository.getTheme()) }
    var autoConnect by remember { mutableStateOf(settingsRepository.isAutoConnectEnabled()) }
    var notifications by remember { mutableStateOf(settingsRepository.areNotificationsEnabled()) }
    var onboardingDone by remember { mutableStateOf(settingsRepository.isOnboardingCompleted()) }

    // Применяем тему
    LaunchedEffect(visualTheme) {
        CyberColors.setTheme(visualTheme)
    }

    // Сохраняем настройки
    LaunchedEffect(lang) { settingsRepository.setLanguage(lang) }
    LaunchedEffect(selectedMode) { settingsRepository.setSelectedMode(selectedMode) }
    LaunchedEffect(selectedServer) { settingsRepository.setSelectedServer(selectedServer) }
    LaunchedEffect(visualTheme) { settingsRepository.setTheme(visualTheme) }
    LaunchedEffect(autoConnect) { settingsRepository.setAutoConnect(autoConnect) }
    LaunchedEffect(notifications) { settingsRepository.setNotifications(notifications) }
    LaunchedEffect(onboardingDone) { settingsRepository.setOnboardingCompleted(onboardingDone) }

    // Таймер сессии
    var sessionSeconds by remember { mutableLongStateOf(0L) }
    val connected = activity.vpnConnected
    val busy = activity.vpnBusy

    LaunchedEffect(connected) {
        if (!connected) {
            sessionSeconds = 0L
            return@LaunchedEffect
        }
        while (true) {
            delay(1000)
            sessionSeconds += 1
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(CyberColors.DeepBg, CyberColors.DarkBg)))
        ) {
            CyberBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(22.dp))

                CyberHeader(
                    lang = lang,
                    onLangToggle = { lang = lang.toggle() }
                )

                Spacer(Modifier.height(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (!onboardingDone) {
                        OnboardingScreenV7(
                            lang = lang,
                            onFinish = { onboardingDone = true }
                        )
                    } else {
                        when (screen) {
                            Screen.Home -> HomeScreenV4(
                                lang = lang,
                                connected = connected,
                                busy = busy,
                                statusText = activity.uiStatus,
                                mode = Constants.PROTECTION_MODES[selectedMode],
                                server = Constants.VPN_SERVERS[selectedServer],
                                onPower = { activity.onPowerClick() }
                            )

                            Screen.Modes -> ModesScreenV4(
                                lang = lang,
                                selected = selectedMode,
                                onSelect = { selectedMode = it }
                            )

                            Screen.Servers -> ServersScreenV4(
                                lang = lang,
                                selected = selectedServer,
                                onSelect = { index ->
                                    if (Constants.VPN_SERVERS[index].active) {
                                        selectedServer = index
                                    }
                                }
                            )

                            Screen.Status -> StatusScreenV4(
                                lang = lang,
                                connected = connected,
                                busy = busy,
                                statusText = activity.uiStatus,
                                mode = Constants.PROTECTION_MODES[selectedMode],
                                server = Constants.VPN_SERVERS[selectedServer],
                                sessionSeconds = sessionSeconds,
                                clientName = vpnRepository.getClientName(),
                                accessExpiresAt = vpnRepository.getAccessExpiresAt()
                            )

                            Screen.Settings -> SettingsScreenV4(
                                lang = lang,
                                autoConnect = autoConnect,
                                notifications = notifications,
                                onAutoToggle = { autoConnect = !autoConnect },
                                onNotificationsToggle = { notifications = !notifications },
                                onLangToggle = { lang = lang.toggle() },
                                visualTheme = visualTheme,
                                accessExpiresAt = vpnRepository.getAccessExpiresAt(),
                                onThemeSelect = { visualTheme = it },
                                onReset = { activity.resetAccess() }
                            )
                        }
                    }
                }

                CyberBottomNav(
                    current = screen,
                    lang = lang,
                    onSelect = { screen = it }
                )

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}