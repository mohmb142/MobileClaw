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
private data class ChatBody(
    val model: String,
    val messages: List<AIMessage>,
    val temperature: Double = 0.2,
    val max_tokens: Int = 2048
)

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
        if (!config.enabled) throw AIProviderException.Invalid("${config.name} is disabled")
        if (config.apiKey.isBlank()) throw AIProviderException.Invalid("API key for ${config.name} is empty")

        val selectedModel = model ?: request.model ?: config.defaultModel
        val started = System.currentTimeMillis()

        try {
            val response = client.post(config.baseUrl.trimEnd('/') + "/chat/completions") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
                setBody(ChatBody(selectedModel, request.messages, request.temperature, request.maxTokens))
            }

            if (response.status.value !in 200..299) {
                throw AIProviderException.Http(
                    response.status.value,
                    "${config.name}: HTTP ${response.status.value}"
                )
            }

            val result: ChatResult = response.body()
            val text = result.choices.firstOrNull()?.message?.content?.trim()
                ?: throw AIProviderException.Invalid("${config.name}: empty response")

            return AIResponse(
                text = text,
                provider = id,
                model = selectedModel,
                latencyMs = System.currentTimeMillis() - started
            )
        } catch (error: AIProviderException) {
            throw error
        } catch (error: Exception) {
            throw AIProviderException.Timeout(
                "${config.name}: ${error.message ?: "network error"}",
                error
            )
        }
    }
}
