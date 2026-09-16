package ai.astra.providers

data class AIResponse(
    val text: String,
    val provider: String,
    val model: String,
    val latencyMs: Long,
    val fallbackUsed: Boolean = false
)

sealed class AIProviderException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Http(val code: Int, message: String) : AIProviderException(message)
    class Timeout(message: String, cause: Throwable? = null) : AIProviderException(message, cause)
    class Invalid(message: String, cause: Throwable? = null) : AIProviderException(message, cause)
}
