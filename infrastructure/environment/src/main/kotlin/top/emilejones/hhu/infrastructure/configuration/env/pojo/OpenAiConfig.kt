package top.emilejones.hhu.infrastructure.configuration.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class OpenAiConfig(
    val baseUrl: String,
    val llmUrl: String = baseUrl,
    val token: String = "",
    val llmToken: String = token,
    val rerankModel: String,
    val embeddingModel: String,
    val llmModel: String,
    val dimension: Int
)