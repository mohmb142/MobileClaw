package ai.astra.providers

import io.ktor.client.HttpClient

object AstraProviders {
    fun createRouter(
        client: HttpClient,
        geminiKey: String,
        groqKey: String,
        openRouterKey: String,
        huggingFaceKey: String
    ): AIRouter {
        val list = buildList {
            if (geminiKey.isNotBlank()) add(OpenAICompatibleProvider(client, ProviderConfig("gemini", "Google Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", geminiKey, "gemini-2.5-flash")))
            if (groqKey.isNotBlank()) add(OpenAICompatibleProvider(client, ProviderConfig("groq", "Groq", "https://api.groq.com/openai/v1", groqKey, "llama-3.3-70b-versatile")))
            if (openRouterKey.isNotBlank()) add(OpenAICompatibleProvider(client, ProviderConfig("openrouter", "OpenRouter", "https://openrouter.ai/api/v1", openRouterKey, "openai/gpt-oss-20b:free")))
            if (huggingFaceKey.isNotBlank()) add(OpenAICompatibleProvider(client, ProviderConfig("huggingface", "Hugging Face", "https://router.huggingface.co/v1", huggingFaceKey, "openai/gpt-oss-20b")))
        }
        return AIRouter(list)
    }
}
