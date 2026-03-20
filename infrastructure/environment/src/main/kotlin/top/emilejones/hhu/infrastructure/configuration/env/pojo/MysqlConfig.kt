package top.emilejones.hhu.infrastructure.configuration.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class MysqlConfig(
    val database: String,
    val host: String,
    val port: Int,
    val user: String,
    val password: String
)