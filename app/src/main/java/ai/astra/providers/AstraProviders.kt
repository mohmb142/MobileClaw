package ai.astra.providers

import io.ktor.client.HttpClient

/**
 * Cloud-only provider factory for Astra.
 *
 * Provider IDs intentionally match the existing MobileClaw provider IDs
 * (notably Google uses "google", not "gemini") so preferences and the
 * existing provider catalog remain compatible.
 */
object AstraProviders {
    fun createRouter(
        client: HttpClient,
        googleKey: String,
        groqKey: String,
        openRouterKey: String,
        huggingFaceKey: String
    ): AIRouter {
        val list = buildList {
            if (googleKey.isNotBlank()) {
                add(
                    OpenAICompatibleProvider(
                        client,
                        ProviderConfig(
                            id = "google",
                            name = "Google Gemini",
                            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai",
                            apiKey = googleKey,
                            defaultModel = "gemini-2.5-flash"
                        )
                    )
                )
            }
            if (groqKey.isNotBlank()) {
                add(
                    OpenAICompatibleProvider(
                        client,
                        ProviderConfig(
                            id = "groq",
                            name = "Groq",
                            baseUrl = "https://api.groq.com/openai/v1",
                            apiKey = groqKey,
                            defaultModel = "llama-3.3-70b-versatile"
                        )
                    )
                )
            }
            if (openRouterKey.isNotBlank()) {
                add(
                    OpenAICompatibleProvider(
                        client,
                        ProviderConfig(
                            id = "openrouter",
                            name = "OpenRouter",
                            baseUrl = "https://openrouter.ai/api/v1",
                            apiKey = openRouterKey,
                            defaultModel = "openrouter/free"
                        )
                    )
                )
            }
            if (huggingFaceKey.isNotBlank()) {
                add(
                    OpenAICompatibleProvider(
                        client,
                        ProviderConfig(
                            id = "huggingface",
                            name = "Hugging Face",
                            baseUrl = "https://router.huggingface.co/v1",
                            apiKey = huggingFaceKey,
                            defaultModel = "openai/gpt-oss-20b"
                        )
                    )
                )
            }
        }
        return AIRouter(list)
    }
}
