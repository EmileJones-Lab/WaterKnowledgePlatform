package top.emilejones.hhu.domain.pipeline

import top.emilejones.hhu.domain.AggregateRoot

/**
 * 文本节点，承载切分后的文本内容和层级信息。
 * @author EmileJones
 */
class TextNode(
    val elementId: String,
    val fileNodeElementId: String,
    val text: String,
    val seq: Int,
    val level: Int,
    val type: TextType,
    var isEmbedded: Boolean
): AggregateRoot<String>(elementId) {
}
