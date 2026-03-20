package top.emilejones.hhu.infrastructure.configuration.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class OpenAiConfig(
    val baseUrl: String,
    val token: String? = null,
    val llmUrl: String = baseUrl,
    val embeddingUrl: String = baseUrl,
    val rerankUrl: String = baseUrl,
    val llmToken: String? = token,
    val embeddingToken: String? = token,
    val rerankToken: String? = token,
    val llmModel: String,
    val rerankModel: String,
    val embeddingModel: String,
    val dimension: Int
)
