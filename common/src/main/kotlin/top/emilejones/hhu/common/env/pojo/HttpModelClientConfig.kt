package top.emilejones.hhu.common.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class HttpModelClientConfig(
    val host: String,
    val port: Int,
    val token: String? = null,
    val rerankModel: String,
    val embeddingModel: String,
    val dimension: Int
)