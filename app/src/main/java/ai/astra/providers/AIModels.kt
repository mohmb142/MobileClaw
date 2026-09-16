package ai.astra.providers

import kotlinx.serialization.Serializable

@Serializable
data class AIMessage(val role: String, val content: String)

@Serializable
data class AIRequest(
    val messages: List<AIMessage>,
    val model: String? = null,
    val temperature: Double = 0.2,
    val maxTokens: Int = 2048
)

data class AIResponse(
    val text: String,
    val provider: String,
    val model: String,
    val latencyMs: Long,
    val fallbackUsed: Boolean = false
)

data class ProviderConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val defaultModel: String,
    val enabled: Boolean = true,
    val freeTierOnly: Boolean = true
)

sealed class AIProviderException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Http(val code: Int, message: String) : AIProviderException(message)
    class Timeout(message: String) : AIProviderException(message)
    class Invalid(message: String) : AIProviderException(message)
}
