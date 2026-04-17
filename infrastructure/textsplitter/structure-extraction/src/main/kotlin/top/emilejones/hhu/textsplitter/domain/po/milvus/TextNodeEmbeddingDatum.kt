package top.emilejones.hhu.textsplitter.domain.po.milvus


data class TextNodeEmbeddingDatum(
    val vector: List<Float>,
    val neo4jNodeId: String,
    val fileNodeId: String,
    val isDelete: Boolean = false
)
