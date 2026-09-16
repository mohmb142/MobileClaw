package ai.astra.providers

/**
 * Cloud-only AI router for Astra.
 *
 * Providers are tried in the configured order. A failed provider never blocks
 * the remaining providers, which is important for expired keys, rate limits,
 * temporary outages and free-tier exhaustion.
 */
class AIRouter(
    private val providers: List<AIProvider>
) {
    suspend fun chat(request: AIRequest): AIResponse {
        if (providers.isEmpty()) {
            throw AIProviderException.Invalid("Astra: no cloud AI providers configured")
        }

        var lastError: Throwable? = null
        providers.forEachIndexed { index, provider ->
            try {
                return provider.chat(request).copy(fallbackUsed = index > 0)
            } catch (error: Throwable) {
                lastError = error
            }
        }

        throw AIProviderException.Invalid(
            "All Astra cloud AI providers failed: ${lastError?.message ?: "unknown error"}",
            lastError
        )
    }
}
