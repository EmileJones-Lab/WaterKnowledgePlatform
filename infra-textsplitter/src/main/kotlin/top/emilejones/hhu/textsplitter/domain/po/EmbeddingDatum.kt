package top.emilejones.hhu.textsplitter.domain.po

import top.emilejones.hhu.domain.pipeline.TextType


data class EmbeddingDatum(
    val vector: List<Float>,
    val neo4jNodeId: String,
    val text: String,
    val type: TextType,
    val isDelete: Boolean = false
)
