package top.emilejones.hhu.repository.milvus.po

import top.emilejones.hhu.repository.neo4j.enums.TextType

data class EmbeddingDatum(
    val vector: List<Float>,
    val neo4jElementId: String,
    val text: String,
    val type: TextType
)
