package app.cloavy.vpn

import android.content.Context
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config

class WireGuardController(context: Context) {
    private val backend = GoBackend(context)
    private val tunnel = object : Tunnel {
        override fun getName(): String = "cloavy-real-test"
        override fun onStateChange(newState: Tunnel.State) = Unit
    }

    @Volatile
    private var lastConfig: Config? = null

    fun validateConfig(configText: String) {
        Config.parse(configText.byteInputStream())
    }

    fun connect(configText: String) {
        val config = Config.parse(configText.byteInputStream())
        lastConfig = config
        backend.setState(tunnel, Tunnel.State.UP, config)
    }

    fun disconnect() {
        backend.setState(tunnel, Tunnel.State.DOWN, null)
    }

    fun getBackendVersion(): String = backend.version
}
