package top.emilejones.hhu.textsplitter.domain.po

import top.emilejones.hhu.domain.result.TextType

data class Neo4jTextNode(
    val elementId: String? = null,
    val id: String,
    val text: String,
    val seq: Int,
    val level: Int,
    val type: TextType,
    val vector: List<Float>?,
    val isDelete: Boolean = false,
    val summary: String? = null
) {
    val name: String = seq.toString()
    val length: Int = text.length
}
