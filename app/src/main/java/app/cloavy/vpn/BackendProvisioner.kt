package app.cloavy.vpn

import android.content.Context
import android.provider.Settings
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object CloavyBackendConfig {
    // Test backend running on your Contabo VPS. Later this will be replaced by HTTPS domain.
    const val BASE_URL = "http://169.58.97.115:8080"

    // Test token. Keep the same value in /opt/cloavy-backend/.env on the server.
    // For public testing, change it in both backend and app before sharing APK widely.
    const val API_TOKEN = "cloavy_test_2026_token"
}

data class ProvisionResult(
    val vpnConfig: String,
    val clientName: String,
    val serverLabel: String,
    val reused: Boolean,
    val expiresAt: String
)

class BackendProvisioner(private val context: Context) {
    fun getDeviceId(): String {
        val secureId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return if (!secureId.isNullOrBlank()) {
            "android-$secureId"
        } else {
            val prefs = context.getSharedPreferences("cloavy_device", 0)
            val existing = prefs.getString("device_id", null)
            if (!existing.isNullOrBlank()) return existing
            val generated = "android-${UUID.randomUUID()}"
            prefs.edit().putString("device_id", generated).apply()
            generated
        }
    }

    fun provision(): ProvisionResult {
        val url = URL("${CloavyBackendConfig.BASE_URL}/api/provision")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Cloavy-Test-Token", CloavyBackendConfig.API_TOKEN)
        }

        val body = JSONObject()
            .put("deviceId", getDeviceId())
            .put("platform", "android")
            .put("appVersion", "0.7.0-product-beta")
            .toString()

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }

        val code = conn.responseCode
        val responseText = try {
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            stream.bufferedReader(Charsets.UTF_8).use(BufferedReader::readText)
        } finally {
            conn.disconnect()
        }

        if (code !in 200..299) {
            throw IllegalStateException("Backend error $code: $responseText")
        }

        val json = JSONObject(responseText)
        if (!json.optBoolean("success", false)) {
            throw IllegalStateException(json.optString("error", "Provision failed"))
        }

        val config = json.getString("vpnConfig")
        if (!config.contains("[Interface]") || !config.contains("[Peer]")) {
            throw IllegalStateException("Backend returned invalid WireGuard config")
        }

        return ProvisionResult(
            vpnConfig = config,
            clientName = json.optString("clientName", "cloavy-android"),
            serverLabel = json.optString("serverLabel", "Contabo EU"),
            reused = json.optBoolean("reused", false),
            expiresAt = json.optString("expiresAt", "")
        )
    }
}
