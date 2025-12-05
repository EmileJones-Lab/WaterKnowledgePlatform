package top.emilejones.hhu.env.pojo

import kotlinx.serialization.Serializable


@Serializable
data class MinioConfig(
    val host: String,
    val port: Int,
    val user: String,
    val password: String,
    val bucket: String
)