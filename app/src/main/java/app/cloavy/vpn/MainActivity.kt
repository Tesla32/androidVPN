package app.cloavy.vpn

import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    private lateinit var vpnController: WireGuardController
    private lateinit var provisioner: BackendProvisioner
    private val prefs by lazy { getSharedPreferences("cloavy_vpn_v4", 0) }

    var vpnConnected by mutableStateOf(false)
    var vpnBusy by mutableStateOf(false)
    var vpnConfigLoaded by mutableStateOf(false)
    var uiStatus by mutableStateOf("Готово к защите")
    var lastClientName by mutableStateOf("")
    var accessExpiresAt by mutableStateOf("")

    private val vpnPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnTunnel()
        } else {
            vpnBusy = false
            uiStatus = "Разрешение VPN не выдано"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnController = WireGuardController(applicationContext)
        provisioner = BackendProvisioner(applicationContext)
        vpnConfigLoaded = !prefs.getString("wg_config", "").isNullOrBlank()
        lastClientName = prefs.getString("wg_client_name", "").orEmpty()
        accessExpiresAt = prefs.getString("access_expires_at", "").orEmpty()
        uiStatus = if (vpnConfigLoaded) "Доступ готов. Можно активировать защиту" else "Нажмите Активировать"
        setContent { CloavyCyberpunkApp() }
    }

    fun onPowerClick() {
        if (vpnBusy) return
        if (vpnConnected) {
            stopVpnTunnel()
        } else {
            lifecycleScope.launch { ensureConfigAndStart() }
        }
    }

    fun resetAccess() {
        prefs.edit().remove("wg_config").remove("wg_client_name").remove("access_expires_at").apply()
        vpnConfigLoaded = false
        lastClientName = ""
        accessExpiresAt = ""
        uiStatus = "Доступ сброшен. Нажмите Активировать"
    }

    private suspend fun ensureConfigAndStart() {
        var configText = prefs.getString("wg_config", "").orEmpty()
        if (configText.isBlank()) {
            vpnBusy = true
            uiStatus = "Получаем персональный доступ"
            val result = runCatching {
                withContext(Dispatchers.IO) { provisioner.provision() }
            }
            result.onSuccess { provision ->
                prefs.edit()
                    .putString("wg_config", provision.vpnConfig)
                    .putString("wg_client_name", provision.clientName)
                    .putString("server_label", provision.serverLabel)
                    .putString("access_expires_at", provision.expiresAt.ifBlank { defaultAccessExpiresAtIso() })
                    .apply()
                vpnConfigLoaded = true
                lastClientName = provision.clientName
                accessExpiresAt = provision.expiresAt.ifBlank { defaultAccessExpiresAtIso() }
                configText = provision.vpnConfig
                uiStatus = "Доступ готов"
            }.onFailure { error ->
                vpnBusy = false
                vpnConnected = false
                uiStatus = "Сервер временно недоступен: ${error.message ?: "ошибка"}"
                return
            }
            vpnBusy = false
        }

        val permissionIntent = VpnService.prepare(this@MainActivity)
        if (permissionIntent != null) {
            vpnBusy = true
            uiStatus = "Подтвердите VPN-разрешение Android"
            vpnPermissionLauncher.launch(permissionIntent)
        } else {
            startVpnTunnel()
        }
    }

    private fun startVpnTunnel() {
        val configText = prefs.getString("wg_config", "").orEmpty()
        if (configText.isBlank()) {
            vpnBusy = false
            vpnConfigLoaded = false
            uiStatus = "Нет доступа. Нажмите Активировать"
            return
        }
        lifecycleScope.launch {
            vpnBusy = true
            uiStatus = "Подключаем защиту"
            runCatching {
                withContext(Dispatchers.IO) { vpnController.connect(configText) }
            }.onSuccess {
                vpnConnected = true
                uiStatus = "Защита активна"
            }.onFailure { error ->
                vpnConnected = false
                uiStatus = "Не удалось подключиться: ${error.message ?: "ошибка"}"
            }
            vpnBusy = false
        }
    }

    private fun stopVpnTunnel() {
        lifecycleScope.launch {
            vpnBusy = true
            uiStatus = "Отключаем защиту"
            runCatching {
                withContext(Dispatchers.IO) { vpnController.disconnect() }
            }.onSuccess {
                vpnConnected = false
                uiStatus = "Защита выключена"
            }.onFailure { error ->
                uiStatus = "Не удалось отключить: ${error.message ?: "ошибка"}"
            }
            vpnBusy = false
        }
    }
}

enum class AppLang { RU, EN }
enum class VisualTheme { Steppe, Brutal, Minimal, Cyberpunk }
enum class Screen { Home, Modes, Servers, Status, Settings }
enum class VpnVisualState { Off, Busy, On, Error }

data class ProtectionMode(
    val icon: String,
    val ru: String,
    val en: String,
    val ruDesc: String,
    val enDesc: String
)

data class VpnServer(
    val flag: String,
    val ru: String,
    val en: String,
    val city: String,
    val ping: String,
    val active: Boolean
)

private val Modes = listOf(
    ProtectionMode("☕", "Кафе", "Cafe", "Защита в публичных Wi-Fi", "Protection on public Wi-Fi"),
    ProtectionMode("🏦", "Банк", "Bank", "Безопасные платежи и финансы", "Safe payments and finance"),
    ProtectionMode("💼", "Работа", "Work", "Доступ к рабочим сервисам", "Access to work services"),
    ProtectionMode("✈", "Путешествие", "Travel", "Интернет в поездках", "Internet while traveling"),
    ProtectionMode("⚡", "Скорость", "Speed", "Оптимизация соединения", "Connection optimization"),
    ProtectionMode("🛡", "Приватность", "Privacy", "Максимум конфиденциальности", "Maximum privacy")
)

private val Servers = listOf(
    VpnServer("⚡", "Auto", "Auto", "Best route", "auto", true),
    VpnServer("🇪🇺", "Contabo EU", "Contabo EU", "Europe", "72 ms", true),
    VpnServer("🇩🇪", "Германия", "Germany", "Frankfurt", "скоро", false),
    VpnServer("🇳🇱", "Нидерланды", "Netherlands", "Amsterdam", "скоро", false),
    VpnServer("🇺🇸", "США", "USA", "New York", "скоро", false),
    VpnServer("🇯🇵", "Япония", "Japan", "Tokyo", "скоро", false)
)

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

object CyberColors {
    private val CyberpunkPalette = ThemePalette(
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

    private val MinimalPalette = ThemePalette(
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

    private val SteppePalette = ThemePalette(
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

    private val BrutalPalette = ThemePalette(
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

    var palette: ThemePalette = MinimalPalette

    fun setTheme(theme: VisualTheme) {
        palette = when (theme) {
            VisualTheme.Steppe -> SteppePalette
            VisualTheme.Brutal -> BrutalPalette
            VisualTheme.Minimal -> MinimalPalette
            VisualTheme.Cyberpunk -> CyberpunkPalette
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

@Composable
fun CloavyCyberpunkApp() {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val prefs = remember { context.getSharedPreferences("cloavy_v4_ui", 0) }
    var lang by remember { mutableStateOf(if (prefs.getString("lang", "ru") == "en") AppLang.EN else AppLang.RU) }
    var screen by remember { mutableStateOf(Screen.Home) }
    var selectedMode by remember { mutableIntStateOf(prefs.getInt("mode", 0)) }
    var selectedServer by remember { mutableIntStateOf(prefs.getInt("server", 0)) }
    var visualTheme by remember { mutableStateOf(VisualTheme.values().getOrElse(prefs.getInt("theme", VisualTheme.Minimal.ordinal)) { VisualTheme.Minimal }) }
    CyberColors.setTheme(visualTheme)
    var autoConnect by remember { mutableStateOf(prefs.getBoolean("auto_connect", false)) }
    var notifications by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var onboardingDone by remember { mutableStateOf(prefs.getBoolean("onboarding_done", false)) }

    LaunchedEffect(lang) { prefs.edit().putString("lang", if (lang == AppLang.RU) "ru" else "en").apply() }
    LaunchedEffect(selectedMode) { prefs.edit().putInt("mode", selectedMode).apply() }
    LaunchedEffect(selectedServer) { prefs.edit().putInt("server", selectedServer).apply() }
    LaunchedEffect(visualTheme) { prefs.edit().putInt("theme", visualTheme.ordinal).apply(); CyberColors.setTheme(visualTheme) }
    LaunchedEffect(autoConnect) { prefs.edit().putBoolean("auto_connect", autoConnect).apply() }
    LaunchedEffect(notifications) { prefs.edit().putBoolean("notifications", notifications).apply() }
    LaunchedEffect(onboardingDone) { prefs.edit().putBoolean("onboarding_done", onboardingDone).apply() }

    var sessionSeconds by remember { mutableLongStateOf(0L) }
    val connected = activity?.vpnConnected == true
    val busy = activity?.vpnBusy == true
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
                CyberHeader(lang = lang, onLangToggle = { lang = if (lang == AppLang.RU) AppLang.EN else AppLang.RU })
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (!onboardingDone) {
                        OnboardingScreenV7(
                            lang = lang,
                            onFinish = { onboardingDone = true }
                        )
                    } else when (screen) {
                        Screen.Home -> HomeScreenV4(
                            lang = lang,
                            connected = connected,
                            busy = busy,
                            statusText = activity?.uiStatus.orEmpty(),
                            mode = Modes[selectedMode],
                            server = Servers[selectedServer],
                            onPower = { activity?.onPowerClick() }
                        )
                        Screen.Modes -> ModesScreenV4(
                            lang = lang,
                            selected = selectedMode,
                            onSelect = { selectedMode = it }
                        )
                        Screen.Servers -> ServersScreenV4(
                            lang = lang,
                            selected = selectedServer,
                            onSelect = { index -> if (Servers[index].active) selectedServer = index }
                        )
                        Screen.Status -> StatusScreenV4(
                            lang = lang,
                            connected = connected,
                            busy = busy,
                            statusText = activity?.uiStatus.orEmpty(),
                            mode = Modes[selectedMode],
                            server = Servers[selectedServer],
                            sessionSeconds = sessionSeconds,
                            clientName = activity?.lastClientName.orEmpty(),
                            accessExpiresAt = activity?.accessExpiresAt.orEmpty()
                        )
                        Screen.Settings -> SettingsScreenV4(
                            lang = lang,
                            autoConnect = autoConnect,
                            notifications = notifications,
                            onAutoToggle = { autoConnect = !autoConnect },
                            onNotificationsToggle = { notifications = !notifications },
                            onLangToggle = { lang = if (lang == AppLang.RU) AppLang.EN else AppLang.RU },
                            visualTheme = visualTheme,
                            accessExpiresAt = activity?.accessExpiresAt.orEmpty(),
                            onThemeSelect = { visualTheme = it },
                            onReset = { activity?.resetAccess() }
                        )
                    }
                }
                CyberBottomNav(current = screen, lang = lang, onSelect = { screen = it })
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun CyberBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = CyberColors.PrimaryNeon.copy(alpha = 0.14f),
            radius = size.minDimension * 0.45f,
            center = Offset(size.width * 0.88f, size.height * 0.12f)
        )
        drawCircle(
            color = CyberColors.SecondaryNeon.copy(alpha = 0.10f),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.10f, size.height * 0.78f)
        )
        for (i in 0..8) {
            val x = size.width * (0.05f + i * 0.13f)
            drawLine(
                color = CyberColors.PrimaryNeon.copy(alpha = 0.06f),
                start = Offset(x, 0f),
                end = Offset(x + 120f, size.height),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun CyberHeader(lang: AppLang, onLangToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(CyberColors.PrimaryNeon, CyberColors.SecondaryNeon))),
            contentAlignment = Alignment.Center
        ) {
            Text(CyberColors.ThemeEmoji, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Cloavy VPN", color = CyberColors.TextPrimary, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text(if (lang == AppLang.RU) "Один тап - и ты под защитой" else "One tap - and you are protected", color = CyberColors.TextSecondary, fontSize = 12.sp)
        }
        NeonSmallButton(text = if (lang == AppLang.RU) "EN" else "RU", onClick = onLangToggle)
    }
}

@Composable
fun HomeScreenV4(
    lang: AppLang,
    connected: Boolean,
    busy: Boolean,
    statusText: String,
    mode: ProtectionMode,
    server: VpnServer,
    onPower: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            ShieldHeroCard(
                lang = lang,
                connected = connected,
                busy = busy,
                statusText = statusText,
                onPower = onPower
            )
        }
        item {
            InfoStackCard(
                rows = listOf(
                    Triple("📶", if (lang == AppLang.RU) "Сеть" else "Network", if (lang == AppLang.RU) "Public Wi-Fi" else "Public Wi-Fi"),
                    Triple("🛡", if (lang == AppLang.RU) "Риск" else "Risk", if (connected) { if (lang == AppLang.RU) "Низкий" else "Low" } else { if (lang == AppLang.RU) "Высокий" else "High" }),
                    Triple("🌐", if (lang == AppLang.RU) "Сервер" else "Server", if (lang == AppLang.RU) server.ru else server.en),
                    Triple(mode.icon, if (lang == AppLang.RU) "Режим" else "Mode", if (lang == AppLang.RU) mode.ru else mode.en)
                )
            )
        }
        item {
            CyberCard {
                Text(if (lang == AppLang.RU) "Что делает Cloavy" else "What Cloavy does", color = CyberColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                FeatureLine("🧬", if (lang == AppLang.RU) "Создает персональный VPN-доступ автоматически" else "Creates personal VPN access automatically")
                FeatureLine("⚡", if (lang == AppLang.RU) "Подключается одной кнопкой" else "Connects with one button")
                FeatureLine("🔒", if (lang == AppLang.RU) "Скрывает сложную техническую часть" else "Hides complex technical setup")
            }
        }
    }
}

@Composable
fun ShieldHeroCard(lang: AppLang, connected: Boolean, busy: Boolean, statusText: String, onPower: () -> Unit) {
    val visualState = when {
        busy -> VpnVisualState.Busy
        connected -> VpnVisualState.On
        statusText.lowercase().contains("ошиб") || statusText.lowercase().contains("error") -> VpnVisualState.Error
        else -> VpnVisualState.Off
    }
    CyberCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            ShieldCoreButton(visualState = visualState, onClick = onPower)
            Spacer(Modifier.height(8.dp))
            val title = when (visualState) {
                VpnVisualState.On -> if (lang == AppLang.RU) "Защита активна" else "Protection active"
                VpnVisualState.Busy -> if (lang == AppLang.RU) "Подключение" else "Connecting"
                VpnVisualState.Error -> if (lang == AppLang.RU) "Нужна проверка" else "Check required"
                VpnVisualState.Off -> if (lang == AppLang.RU) "Не защищен" else "Not protected"
            }
            Text(title, color = CyberColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(cleanStatus(statusText, lang), color = CyberColors.TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(18.dp))
            NeonButton(
                text = when (visualState) {
                    VpnVisualState.On -> if (lang == AppLang.RU) "ОТКЛЮЧИТЬ" else "DISCONNECT"
                    VpnVisualState.Busy -> if (lang == AppLang.RU) "ПОДКЛЮЧАЕМ" else "CONNECTING"
                    else -> if (lang == AppLang.RU) "АКТИВИРОВАТЬ" else "ACTIVATE"
                },
                enabled = !busy,
                onClick = onPower
            )
        }
    }
}

@Composable
fun ShieldCoreButton(visualState: VpnVisualState, onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "shield")
    val pulse by infinite.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(1300), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )
    val sweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1600), repeatMode = RepeatMode.Restart),
        label = "sweep"
    )
    val activeColor by animateColorAsState(
        targetValue = when (visualState) {
            VpnVisualState.On -> CyberColors.Success
            VpnVisualState.Error -> CyberColors.Danger
            else -> CyberColors.PrimaryNeon
        },
        label = "color"
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(238.dp)) {
        Canvas(modifier = Modifier.size(230.dp)) {
            drawCircle(activeColor.copy(alpha = 0.10f * pulse), radius = size.minDimension * 0.48f)
            drawCircle(CyberColors.SecondaryNeon.copy(alpha = 0.18f), radius = size.minDimension * 0.42f, style = Stroke(width = 4f))
            drawArc(activeColor.copy(alpha = 0.96f), -90f + if (visualState == VpnVisualState.Busy) sweep else 0f, 260f, false, style = Stroke(width = 8f, cap = StrokeCap.Round))
            drawArc(CyberColors.SecondaryNeon.copy(alpha = 0.75f), 200f, 110f, false, style = Stroke(width = 5f, cap = StrokeCap.Round))
        }
        Box(
            modifier = Modifier
                .size(152.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(activeColor.copy(alpha = 0.34f), CyberColors.Surface.copy(alpha = 0.95f))))
                .border(1.dp, activeColor.copy(alpha = 0.85f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (visualState == VpnVisualState.On) "🛡" else "⏻", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                Text(
                    text = when (visualState) {
                        VpnVisualState.On -> "ONLINE"
                        VpnVisualState.Busy -> "SYNC"
                        VpnVisualState.Error -> "RETRY"
                        VpnVisualState.Off -> "START"
                    },
                    color = CyberColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ModesScreenV4(lang: AppLang, selected: Int, onSelect: (Int) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenTitle(if (lang == AppLang.RU) "Режимы защиты" else "Protection modes", if (lang == AppLang.RU) "Выберите сценарий под свою ситуацию" else "Choose a scenario for your situation") }
        items(Modes.indices.toList()) { index ->
            val item = Modes[index]
            SelectableCyberRow(
                icon = item.icon,
                title = if (lang == AppLang.RU) item.ru else item.en,
                desc = if (lang == AppLang.RU) item.ruDesc else item.enDesc,
                selected = selected == index,
                enabled = true,
                trailing = if (selected == index) "✓" else "○",
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
fun ServersScreenV4(lang: AppLang, selected: Int, onSelect: (Int) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenTitle(if (lang == AppLang.RU) "Серверы" else "Servers", if (lang == AppLang.RU) "Сейчас активен один тестовый сервер" else "One test server is active now") }
        items(Servers.indices.toList()) { index ->
            val item = Servers[index]
            SelectableCyberRow(
                icon = item.flag,
                title = if (lang == AppLang.RU) item.ru else item.en,
                desc = "${item.city} • ${serverPingText(item.ping, lang)}",
                selected = selected == index,
                enabled = item.active,
                trailing = if (item.active) { if (selected == index) "✓" else "›" } else "soon",
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
fun StatusScreenV4(
    lang: AppLang,
    connected: Boolean,
    busy: Boolean,
    statusText: String,
    mode: ProtectionMode,
    server: VpnServer,
    sessionSeconds: Long,
    clientName: String,
    accessExpiresAt: String
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenTitle(if (lang == AppLang.RU) "Статус" else "Status", if (lang == AppLang.RU) "Детали текущей сессии" else "Current session details") }
        item {
            CyberCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle((if (connected) CyberColors.Success else CyberColors.PrimaryNeon).copy(alpha = 0.18f), radius = size.minDimension * 0.48f)
                            drawArc(if (connected) CyberColors.Success else CyberColors.PrimaryNeon, -90f, if (connected) 360f else 180f, false, style = Stroke(width = 7f, cap = StrokeCap.Round))
                        }
                        Text(if (connected) "✓" else if (busy) "…" else "!", color = CyberColors.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (connected) { if (lang == AppLang.RU) "Защита включена" else "Protection enabled" } else { if (lang == AppLang.RU) "Защита выключена" else "Protection disabled" }, color = CyberColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(cleanStatus(statusText, lang), color = CyberColors.TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
        item {
            InfoStackCard(
                rows = listOf(
                    Triple("⏱", if (lang == AppLang.RU) "Время" else "Time", formatDuration(sessionSeconds)),
                    Triple(mode.icon, if (lang == AppLang.RU) "Режим" else "Mode", if (lang == AppLang.RU) mode.ru else mode.en),
                    Triple(server.flag, if (lang == AppLang.RU) "Сервер" else "Server", if (lang == AppLang.RU) server.ru else server.en),
                    Triple("🔑", if (lang == AppLang.RU) "Доступ" else "Access", accessLabel(accessExpiresAt, lang)),
                    Triple("🧩", if (lang == AppLang.RU) "Клиент" else "Client", if (clientName.isBlank()) "auto" else clientName.take(22))
                )
            )
        }
        item {
            CyberCard {
                Text(if (lang == AppLang.RU) "Трафик за сессию" else "Session traffic", color = CyberColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatBox(modifier = Modifier.weight(1f), label = if (lang == AppLang.RU) "Принято" else "Received", value = if (connected) "считаем" else "0 МБ")
                    StatBox(modifier = Modifier.weight(1f), label = if (lang == AppLang.RU) "Отправлено" else "Sent", value = if (connected) "считаем" else "0 МБ")
                }
                Spacer(Modifier.height(8.dp))
                Text(if (lang == AppLang.RU) "В v4 трафик пока не считаем точно, чтобы не показывать фейковые цифры." else "In v4 traffic is not calculated precisely yet, so we do not show fake numbers.", color = CyberColors.TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SettingsScreenV4(
    lang: AppLang,
    autoConnect: Boolean,
    notifications: Boolean,
    onAutoToggle: () -> Unit,
    onNotificationsToggle: () -> Unit,
    onLangToggle: () -> Unit,
    visualTheme: VisualTheme,
    accessExpiresAt: String,
    onThemeSelect: (VisualTheme) -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenTitle(if (lang == AppLang.RU) "Настройки" else "Settings", if (lang == AppLang.RU) "Гибкая настройка защиты" else "Flexible protection setup") }
        item { ToggleCyberRow("🚀", if (lang == AppLang.RU) "Автоподключение" else "Auto connect", if (lang == AppLang.RU) "Подключаться при запуске" else "Connect on app start", autoConnect, onAutoToggle) }
        item { ToggleCyberRow("🔔", if (lang == AppLang.RU) "Уведомления" else "Notifications", if (lang == AppLang.RU) "Показывать статус защиты" else "Show protection status", notifications, onNotificationsToggle) }
        item { SettingsActionRow("🌐", if (lang == AppLang.RU) "Язык" else "Language", if (lang == AppLang.RU) "Русский" else "English", onLangToggle) }
        item { ThemePicker(lang = lang, selected = visualTheme, onSelect = onThemeSelect) }
        item { AccessStatusCard(lang = lang, accessExpiresAt = accessExpiresAt) }
        item { SettingsActionRow("🧯", if (lang == AppLang.RU) "Сбросить тестовый доступ" else "Reset test access", if (lang == AppLang.RU) "Создать новый доступ при следующем запуске" else "Create new access on next start", onReset) }
        item { SettingsActionRow("🛟", if (lang == AppLang.RU) "Поддержка" else "Support", if (lang == AppLang.RU) "Скоро" else "Soon", {}) }
        item { SettingsActionRow("ℹ", if (lang == AppLang.RU) "О приложении" else "About", "Cloavy VPN v7", {}) }
    }
}


@Composable
fun OnboardingScreenV7(lang: AppLang, onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val slides = if (lang == AppLang.RU) {
        listOf(
            Triple("🛡", "Защита в один тап", "Cloavy сам создает персональный VPN-доступ и подключает защиту без QR-кодов и файлов."),
            Triple("⚡", "Сценарии под задачу", "Кафе, банк, работа, поездки, скорость и приватность — выбирайте режим под ситуацию."),
            Triple("🔑", "Тестовый доступ", "Для закрытого теста доступ выдается автоматически. После проверки подключим оплату и личные права.")
        )
    } else {
        listOf(
            Triple("🛡", "One-tap protection", "Cloavy creates personal VPN access and connects protection without QR codes or config files."),
            Triple("⚡", "Modes for your task", "Cafe, bank, work, travel, speed and privacy — choose the mode for the situation."),
            Triple("🔑", "Test access", "For closed testing, access is issued automatically. Payments and account rights will come next.")
        )
    }
    val slide = slides[page]
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenTitle(if (lang == AppLang.RU) "Добро пожаловать" else "Welcome", if (lang == AppLang.RU) "Быстрый старт Cloavy VPN" else "Quick start with Cloavy VPN") }
        item {
            CyberCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(slide.first, fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(slide.second, color = CyberColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Text(slide.third, color = CyberColors.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    Spacer(Modifier.height(22.dp))
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        slides.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (index == page) 26.dp else 8.dp, 8.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(if (index == page) CyberColors.SecondaryNeon else CyberColors.Border)
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
                        modifier = Modifier.clickable { onFinish() }.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AccessStatusCard(lang: AppLang, accessExpiresAt: String) {
    CyberCard {
        Text(if (lang == AppLang.RU) "Тестовый доступ" else "Test access", color = CyberColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        FeatureLine("🔑", accessLabel(accessExpiresAt, lang))
        FeatureLine("🧪", if (lang == AppLang.RU) "Режим закрытого тестирования" else "Closed test mode")
        FeatureLine("🛒", if (lang == AppLang.RU) "Подписки добавим на следующем этапе" else "Subscriptions will be added at the next stage")
    }
}

@Composable
fun ThemePicker(lang: AppLang, selected: VisualTheme, onSelect: (VisualTheme) -> Unit) {
    CyberCard {
        Text(if (lang == AppLang.RU) "Тема оформления" else "Visual theme", color = CyberColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        VisualTheme.values().forEachIndexed { index, theme ->
            val active = selected == theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) CyberColors.SurfaceLight.copy(alpha = 0.90f) else Color.Transparent)
                    .clickable { onSelect(theme) }
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(themeIcon(theme), fontSize = 22.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Column(modifier = Modifier.weight(1f)) {
                    Text(themeName(theme, lang), color = CyberColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(themeDescription(theme, lang), color = CyberColors.TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(if (active) "✓" else "", color = CyberColors.SecondaryNeon, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            if (index != VisualTheme.values().lastIndex) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CyberColors.Border.copy(alpha = 0.35f)))
            }
        }
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
        Text(title, color = CyberColors.TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = CyberColors.TextSecondary, fontSize = 13.sp)
    }
}

@Composable
fun CyberBottomNav(current: Screen, lang: AppLang, onSelect: (Screen) -> Unit) {
    val labels = listOf(
        Screen.Home to Pair("⌂", if (lang == AppLang.RU) "Главная" else "Home"),
        Screen.Modes to Pair("☷", if (lang == AppLang.RU) "Режимы" else "Modes"),
        Screen.Servers to Pair("◎", if (lang == AppLang.RU) "Серверы" else "Servers"),
        Screen.Status to Pair("◉", if (lang == AppLang.RU) "Статус" else "Status"),
        Screen.Settings to Pair("⚙", if (lang == AppLang.RU) "Настройки" else "Settings")
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CyberColors.Surface.copy(alpha = 0.94f))
            .border(1.dp, CyberColors.Border, RoundedCornerShape(24.dp))
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEach { item ->
            val active = current == item.first
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(item.first) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(item.second.first, color = if (active) CyberColors.SecondaryNeon else CyberColors.TextMuted, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(item.second.second, color = if (active) CyberColors.SecondaryNeon else CyberColors.TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun CyberCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberColors.Border, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberColors.Surface.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(CyberColors.Surface.copy(alpha = 0.96f), CyberColors.SurfaceLight.copy(alpha = 0.55f))))
                .padding(18.dp)
        ) { content() }
    }
}

@Composable
fun InfoStackCard(rows: List<Triple<String, String, String>>) {
    CyberCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEachIndexed { index, row ->
                InfoLine(icon = row.first, title = row.second, value = row.third)
                if (index != rows.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CyberColors.Border.copy(alpha = 0.55f)))
                }
            }
        }
    }
}

@Composable
fun InfoLine(icon: String, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, color = CyberColors.SecondaryNeon, fontSize = 22.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = CyberColors.TextMuted, fontSize = 11.sp)
            Text(value, color = CyberColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Text("›", color = CyberColors.TextMuted, fontSize = 22.sp)
    }
}

@Composable
fun SelectableCyberRow(icon: String, title: String, desc: String, selected: Boolean, enabled: Boolean, trailing: String, onClick: () -> Unit) {
    val border = if (selected) CyberColors.PrimaryNeon else CyberColors.Border
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .border(1.dp, border, RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberColors.Surface.copy(alpha = if (enabled) 0.94f else 0.50f)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background((if (selected) CyberColors.PrimaryNeon else CyberColors.SurfaceLight).copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) { Text(icon, fontSize = 24.sp, color = CyberColors.TextPrimary) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = CyberColors.TextPrimary.copy(alpha = if (enabled) 1f else 0.45f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(desc, color = CyberColors.TextSecondary.copy(alpha = if (enabled) 1f else 0.45f), fontSize = 12.sp)
            }
            Text(trailing, color = if (selected) CyberColors.SecondaryNeon else CyberColors.TextMuted, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun NeonButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = CyberColors.SurfaceLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(CyberColors.PrimaryNeon, CyberColors.AccentPink, CyberColors.SecondaryNeon))),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun NeonSmallButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(CyberColors.Surface.copy(alpha = 0.95f))
            .border(1.dp, CyberColors.Border, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = CyberColors.SecondaryNeon, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun FeatureLine(icon: String, text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(icon, fontSize = 19.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
        Text(text, color = CyberColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f), lineHeight = 17.sp)
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CyberColors.SurfaceLight.copy(alpha = 0.72f))
            .border(1.dp, CyberColors.Border, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text(label, color = CyberColors.TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = CyberColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ToggleCyberRow(icon: String, title: String, desc: String, checked: Boolean, onToggle: () -> Unit) {
    CyberCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 22.sp, modifier = Modifier.width(34.dp), textAlign = TextAlign.Center)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = CyberColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(desc, color = CyberColors.TextMuted, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = CyberColors.TextPrimary, checkedTrackColor = CyberColors.PrimaryNeon)
            )
        }
    }
}

@Composable
fun SettingsActionRow(icon: String, title: String, value: String, onClick: () -> Unit) {
    CyberCard {
        Row(modifier = Modifier.clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 22.sp, modifier = Modifier.width(34.dp), textAlign = TextAlign.Center)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = CyberColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(value, color = CyberColors.TextMuted, fontSize = 12.sp)
            }
            Text("›", color = CyberColors.TextMuted, fontSize = 24.sp)
        }
    }
}


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
    VisualTheme.Steppe -> if (lang == AppLang.RU) "теплый песок, бирюза и степной оберег" else "warm sand, turquoise and steppe shield"
    VisualTheme.Brutal -> if (lang == AppLang.RU) "черный, кислотный и дерзкий" else "black, acid and bold"
    VisualTheme.Minimal -> if (lang == AppLang.RU) "чистый premium, спокойная защита" else "clean premium, calm protection"
    VisualTheme.Cyberpunk -> if (lang == AppLang.RU) "неон, игровой ритм и драйв" else "neon, gaming rhythm and drive"
}


fun defaultAccessExpiresAtIso(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(System.currentTimeMillis() + 7L * 24L * 60L * 60L * 1000L))
}

fun accessLabel(expiresAt: String, lang: AppLang): String {
    if (expiresAt.isBlank()) return if (lang == AppLang.RU) "Будет выдан автоматически" else "Will be issued automatically"
    val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    input.timeZone = TimeZone.getTimeZone("UTC")
    val date = runCatching { input.parse(expiresAt) }.getOrNull()
    if (date == null) return if (lang == AppLang.RU) "Доступ активен" else "Access active"
    val output = if (lang == AppLang.RU) SimpleDateFormat("dd.MM.yyyy", Locale("ru")) else SimpleDateFormat("MMM d, yyyy", Locale.US)
    return if (lang == AppLang.RU) "Активен до ${output.format(date)}" else "Active until ${output.format(date)}"
}

fun cleanStatus(status: String, lang: AppLang): String {
    if (status.isBlank()) return if (lang == AppLang.RU) "Готово" else "Ready"

    if (lang == AppLang.EN) {
        val lower = status.lowercase()
        val translated = when {
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
            else -> status
        }
        return translated
            .replace("WireGuard", "Cloavy")
            .replace("backend", "server")
            .replace("config", "access")
            .replace("ошибка", "error")
            .take(96)
    }

    return status
        .replace("WireGuard", "Cloavy")
        .replace("backend", "сервер")
        .replace("config", "доступ")
        .take(96)
}

fun serverPingText(value: String, lang: AppLang): String {
    return if (lang == AppLang.EN && value == "скоро") "soon" else value
}

fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
