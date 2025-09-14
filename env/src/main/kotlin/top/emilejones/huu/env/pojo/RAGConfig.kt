package top.emilejones.huu.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class RAGConfig(
    val maxSequenceLength: Int,
    val maxTableLength: Int
)