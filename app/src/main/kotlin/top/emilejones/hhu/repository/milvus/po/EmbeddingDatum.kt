package top.emilejones.hhu.repository.milvus.po

data class EmbeddingDatum(
    val vector: List<Float>,
    val neo4jElementId: String,
    val text: String
)
