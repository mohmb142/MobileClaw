package ai.astra.providers

import kotlinx.serialization.Serializable

@Serializable
data class AIRequest(
    val messages: List<AIMessage>,
    val model: String? = null,
    val temperature: Double = 0.2,
    val maxTokens: Int = 2048
)

@Serializable
data class AIMessage(
    val role: String,
    val content: String
)
