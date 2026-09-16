package ai.astra.providers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
private data class ChatBody(val model: String, val messages: List<AIMessage>, val temperature: Double = 0.2, val max_tokens: Int = 2048)

@Serializable
private data class ChatChoice(val message: AIMessage)

@Serializable
private data class ChatResult(val choices: List<ChatChoice>)

class OpenAICompatibleProvider(
    private val client: HttpClient,
    private val config: ProviderConfig
) : AIProvider {
    override val id: String = config.id
    override val name: String = config.name

    override suspend fun chat(request: AIRequest, model: String?): AIResponse {
        if (config.apiKey.isBlank()) throw AIProviderException.Invalid("API key for ${config.name} is empty")
        val started = System.currentTimeMillis()
        val result: ChatResult = try {
            client.post(config.baseUrl.trimEnd('/') + "/chat/completions") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
                setBody(ChatBody(model ?: request.model ?: config.defaultModel, request.messages, request.temperature, request.maxTokens))
            }.body()
        } catch (e: Exception) {
            throw AIProviderException.Timeout("${config.name}: ${e.message ?: "network error"}")
        }
        val text = result.choices.firstOrNull()?.message?.content?.trim()
            ?: throw AIProviderException.Invalid("${config.name}: empty response")
        return AIResponse(text, id, model ?: request.model ?: config.defaultModel, System.currentTimeMillis() - started)
    }
}
