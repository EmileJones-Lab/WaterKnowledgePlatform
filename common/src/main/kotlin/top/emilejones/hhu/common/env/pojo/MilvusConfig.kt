package top.emilejones.hhu.common.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class MilvusConfig(
    val database: String,
    val host: String,
    val port: Int
)