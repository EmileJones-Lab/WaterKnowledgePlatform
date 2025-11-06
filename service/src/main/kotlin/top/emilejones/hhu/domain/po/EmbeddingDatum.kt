package top.emilejones.hhu.domain.po

import top.emilejones.hhu.domain.enums.TextType

data class EmbeddingDatum(
    val vector: List<Float>,
    val neo4jElementId: String,
    val text: String,
    val type: TextType
)
