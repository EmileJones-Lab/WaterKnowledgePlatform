package top.emilejones.hhu.common.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class Neo4jConfig(
    val host: String,
    val port: Int,
    val user: String,
    val password: String,
    val database: String
)