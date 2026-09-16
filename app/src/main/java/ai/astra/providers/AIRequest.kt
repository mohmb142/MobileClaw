package ai.astra.providers

data class AIRequest(
    val messages: List<AIMessage>,
    val model: String? = null,
    val temperature: Double = 0.2,
    val maxTokens: Int = 2048
)

data class AIMessage(
    val role: String,
    val content: String
)
