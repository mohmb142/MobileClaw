package ai.astra.providers

interface AIProvider {
    val id: String
    val name: String
    suspend fun chat(request: AIRequest, model: String? = null): AIResponse
}
