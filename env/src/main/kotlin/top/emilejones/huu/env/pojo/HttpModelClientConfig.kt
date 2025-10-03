package top.emilejones.huu.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class HttpModelClientConfig(
    val host: String,
    val port: Int,
    val token: String,
    val rerankModel: String,
    val embeddingModel: String
)