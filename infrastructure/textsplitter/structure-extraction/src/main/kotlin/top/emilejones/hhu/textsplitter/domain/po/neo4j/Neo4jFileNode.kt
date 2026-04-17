package top.emilejones.hhu.textsplitter.domain.po.neo4j

data class Neo4jFileNode(
    val id: String,
    val fileId: String,
    val isEmbedded: Boolean,
    val isDelete: Boolean = false,
    val fileAbstract: String? = null,
    val vector: List<Float>? = null
)
