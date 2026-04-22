package top.emilejones.hhu.common.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class HttpModelClientConfig(
    val embeddingUrl: String,
    val rerankUrl: String,
    val token: String? = null,
    val embeddingToken: String? = token,
    val rerankToken: String? = token,
    val rerankModel: String,
    val embeddingModel: String,
    val dimension: Int
)
