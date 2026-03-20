package top.emilejones.hhu.infrastructure.configuration.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class RAGConfig(
    val maxSentenceLength: Int,
    val maxTableLength: Int,
    val recallNumber: Int
)