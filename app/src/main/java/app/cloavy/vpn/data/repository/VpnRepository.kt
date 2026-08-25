package app.cloavy.vpn.data.repository

import android.content.Context
import android.content.SharedPreferences
import app.cloavy.vpn.WireGuardController
import app.cloavy.vpn.data.network.BackendProvisioner
import app.cloavy.vpn.data.network.ProvisionResult
import app.cloavy.vpn.utils.Constants
import app.cloavy.vpn.utils.TextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VpnRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_VPN, 0)
    private val vpnController = WireGuardController(context)
    private val provisioner = BackendProvisioner(context)

    fun isConfigLoaded(): Boolean = !prefs.getString("wg_config", "").isNullOrBlank()

    fun getClientName(): String = prefs.getString("wg_client_name", "").orEmpty()

    fun getAccessExpiresAt(): String = prefs.getString("access_expires_at", "").orEmpty()

    fun getVpnConfig(): String = prefs.getString("wg_config", "").orEmpty()

    suspend fun provisionAccess(): ProvisionResult = withContext(Dispatchers.IO) {
        val result = provisioner.provision()

        prefs.edit()
            .putString("wg_config", result.vpnConfig)
            .putString("wg_client_name", result.clientName)
            .putString("server_label", result.serverLabel)
            .putString("access_expires_at", result.expiresAt.ifBlank {
                TextUtils.defaultAccessExpiresAtIso()
            })
            .apply()

        result
    }

    suspend fun connectVpn(config: String) = withContext(Dispatchers.IO) {
        vpnController.connect(config)
    }

    suspend fun disconnectVpn() = withContext(Dispatchers.IO) {
        vpnController.disconnect()
    }

    fun resetAccess() {
        prefs.edit()
            .remove("wg_config")
            .remove("wg_client_name")
            .remove("access_expires_at")
            .apply()
    }
}