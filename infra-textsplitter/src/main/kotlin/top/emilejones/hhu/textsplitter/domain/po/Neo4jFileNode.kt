package top.emilejones.hhu.textsplitter.domain.po

data class Neo4jFileNode(
    val elementId: String? = null,
    val id: String,
    val fileId: String,
    val isEmbedded: Boolean
)
