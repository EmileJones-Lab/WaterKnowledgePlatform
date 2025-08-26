package top.emilejones.hhu.repository.neo4j.po

data class Neo4jTextNode(
    val elementId: String? = null,
    val text: String,
    val seq: Int,
    val level: Int
) {
    val name: String = seq.toString()
    val length: Int = text.length
}
