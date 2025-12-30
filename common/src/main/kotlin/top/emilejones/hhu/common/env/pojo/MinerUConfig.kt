package top.emilejones.hhu.common.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class MinerUConfig(
    val host: String,
    val port: Int
)
