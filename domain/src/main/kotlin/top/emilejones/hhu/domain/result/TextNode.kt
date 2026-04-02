package top.emilejones.hhu.domain.result

import top.emilejones.hhu.domain.AggregateRoot
import java.util.*

/**
 * 文本节点，承载切分后的文本内容和层级信息。
 * @author EmileJones
 */
class TextNode(
    override val id: String,
    val fileNodeId: String,
    val text: String,
    val seq: Int,
    val level: Int,
    val type: TextType,
    isEmbedded: Boolean,
    vector: List<Float>?,
    val summary: String? = null
) : AggregateRoot<String>(id) {
    var vector: List<Float>? = vector
        private set
    var isEmbedded: Boolean = isEmbedded
        private set

    fun saveVector(vector: List<Float>) {
        require(!isEmbedded) { "TextNode[$id]节点向量已经被向量化" }
        require(this.vector == null) { "TextNode[$id]节点向量已经存在" }
        this.vector = vector
        this.isEmbedded = true
    }

    companion object {
        fun create(
            fileNodeId: String,
            text: String,
            seq: Int,
            level: Int,
            type: TextType,
            summary: String? = null
        ): TextNode {
            return TextNode(
                id = UUID.randomUUID().toString(),
                fileNodeId = fileNodeId,
                text = text,
                seq = seq,
                level = level,
                type = type,
                isEmbedded = false,
                vector = null,
                summary = summary
            )
        }
    }

}
