package top.emilejones.hhu.domain.po

data class Neo4jFileNode(
    val elementId: String? = null,
    val fileName: String,
    val isEmbedded: Boolean
)
