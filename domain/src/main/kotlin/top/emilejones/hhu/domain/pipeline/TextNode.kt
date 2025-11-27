package top.emilejones.hhu.domain.pipeline

class TextNode(
    val elementId: String,
    val fileNodeElementId: String,
    val text: String,
    val seq: Int,
    val level: Int,
    val type: TextType,
    var isEmbedded: Boolean
) {
}
