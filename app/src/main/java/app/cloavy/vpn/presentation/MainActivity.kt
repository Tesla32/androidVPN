package app.cloavy.vpn.presentation

import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import app.cloavy.vpn.data.repository.SettingsRepository
import app.cloavy.vpn.data.repository.VpnRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var vpnRepository: VpnRepository
    private lateinit var settingsRepository: SettingsRepository

    var vpnConnected by mutableStateOf(false)
    var vpnBusy by mutableStateOf(false)
    var uiStatus by mutableStateOf("")

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnTunnel()
        } else {
            vpnBusy = false
            uiStatus = "Разрешение VPN не выдано"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vpnRepository = VpnRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)

        uiStatus = if (vpnRepository.isConfigLoaded()) {
            "Доступ готов. Можно активировать защиту"
        } else {
            "Нажмите Активировать"
        }

        setContent {
            CloavyApp(
                activity = this,
                vpnRepository = vpnRepository,
                settingsRepository = settingsRepository
            )
        }
    }

    fun onPowerClick() {
        if (vpnBusy) return

        if (vpnConnected) {
            stopVpnTunnel()
        } else {
            lifecycleScope.launch {
                ensureConfigAndStart()
            }
        }
    }

    fun resetAccess() {
        vpnRepository.resetAccess()
        uiStatus = "Доступ сброшен. Нажмите Активировать"
    }

    private suspend fun ensureConfigAndStart() {
        var configText = vpnRepository.getVpnConfig()

        if (configText.isBlank()) {
            vpnBusy = true
            uiStatus = "Получаем персональный доступ"

            val result = runCatching {
                vpnRepository.provisionAccess()
            }

            result.onSuccess { provision ->
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
        val configText = vpnRepository.getVpnConfig()
        if (configText.isBlank()) {
            vpnBusy = false
            uiStatus = "Нет доступа. Нажмите Активировать"
            return
        }

        lifecycleScope.launch {
            vpnBusy = true
            uiStatus = "Подключаем защиту"

            runCatching {
                vpnRepository.connectVpn(configText)
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
                vpnRepository.disconnectVpn()
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