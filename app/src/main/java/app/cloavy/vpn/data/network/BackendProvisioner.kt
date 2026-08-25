package app.cloavy.vpn.data.network

import android.content.Context
import android.provider.Settings
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object CloavyBackendConfig {
    const val BASE_URL = "http://169.58.97.115:8080"
    const val API_TOKEN = "cloavy_test_2026_token"
}

class BackendProvisioner(private val context: Context) {

    fun provision(): ProvisionResult {
        val json = postJson(
            url = "${CloavyBackendConfig.BASE_URL}/api/provision",
            token = CloavyBackendConfig.API_TOKEN,
            body = JSONObject()
                .put("deviceId", getDeviceId())
                .put("platform", "android")
                .put("appVersion", "0.7.0-product-beta")
        )

        if (!json.optBoolean("success", false)) {
            throw ProvisionException(json.optString("error", "Provision failed"))
        }

        val config = json.getString("vpnConfig")
        if (!config.contains("[Interface]") || !config.contains("[Peer]")) {
            throw ProvisionException("Invalid WireGuard config")
        }

        return ProvisionResult(
            vpnConfig = config,
            clientName = json.optString("clientName", "cloavy-android"),
            serverLabel = json.optString("serverLabel", "Contabo EU"),
            reused = json.optBoolean("reused", false),
            expiresAt = json.optString("expiresAt", "")
        )
    }

    private fun getDeviceId(): String {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?.takeIf { it.isNotBlank() }
            ?.let { return "android-$it" }

        val prefs = context.getSharedPreferences("cloavy_device", 0)
        prefs.getString("device_id", null)?.let { return it }

        return "android-${UUID.randomUUID()}".also {
            prefs.edit().putString("device_id", it).apply()
        }
    }

    private fun postJson(url: String, token: String, body: JSONObject): JSONObject {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Cloavy-Test-Token", token)
        }

        return try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                .bufferedReader(Charsets.UTF_8)
                .use(BufferedReader::readText)

            if (code !in 200..299) {
                throw ProvisionException("Server error $code: ${text.take(200)}")
            }
            JSONObject(text)
        } catch (e: IOException) {
            throw ProvisionException("Network error: ${e.message}", e)
        } finally {
            conn.disconnect()
        }
    }
}