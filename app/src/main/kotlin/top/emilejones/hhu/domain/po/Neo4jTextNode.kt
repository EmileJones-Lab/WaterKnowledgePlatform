package top.emilejones.hhu.domain.po

import top.emilejones.hhu.domain.enums.TextType

data class Neo4jTextNode(
    val elementId: String? = null,
    val text: String,
    val seq: Int,
    val level: Int,
    val type: TextType
) {
    val name: String = seq.toString()
    val length: Int = text.length
}
