package top.emilejones.hhu.textsplitter.domain.po.milvus

/**
 * 文件节点在向量数据库中的存储格式。
 */
data class FileNodeEmbeddingDatum(
    val vector: List<Float>,
    val fileNodeId: String,
    val isDelete: Boolean = false
)
