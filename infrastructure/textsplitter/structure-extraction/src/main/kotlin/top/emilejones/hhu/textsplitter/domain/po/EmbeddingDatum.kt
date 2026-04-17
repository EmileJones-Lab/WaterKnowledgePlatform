package top.emilejones.hhu.textsplitter.domain.po


data class EmbeddingDatum(
    val vector: List<Float>,
    val neo4jNodeId: String,
    val fileNodeId: String,
    val isDelete: Boolean = false
)
