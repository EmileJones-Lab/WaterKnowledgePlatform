package top.emilejones.huu.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class MilvusConfig(
    val database: String,
    val collection: String,
    val host: String,
    val port: Int
)