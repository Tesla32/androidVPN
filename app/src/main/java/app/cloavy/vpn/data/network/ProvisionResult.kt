package app.cloavy.vpn.data.network

data class ProvisionResult(
    val vpnConfig: String,
    val clientName: String,
    val serverLabel: String,
    val reused: Boolean,
    val expiresAt: String
)

class ProvisionException(message: String, cause: Throwable? = null) : Exception(message, cause)