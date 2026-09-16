package ai.astra.providers

class AIRouter(
    private val providers: List<AIProvider>,
    private val isRetryable: (Throwable) -> Boolean = { error ->
        error is AIProviderException.Http && error.code in setOf(408, 409, 425, 429, 500, 502, 503, 504) ||
            error is AIProviderException.Timeout
    }
) {
    suspend fun chat(request: AIRequest): AIResponse {
        if (providers.isEmpty()) throw AIProviderException.Invalid("Astra: no AI providers configured")
        var lastError: Throwable? = null
        providers.forEachIndexed { index, provider ->
            try {
                return provider.chat(request).copy(fallbackUsed = index > 0)
            } catch (error: Throwable) {
                lastError = error
                if (!isRetryable(error) && index == 0) throw error
            }
        }
        throw AIProviderException.Invalid("All Astra AI providers failed: ${lastError?.message ?: "unknown error"}")
    }
}
