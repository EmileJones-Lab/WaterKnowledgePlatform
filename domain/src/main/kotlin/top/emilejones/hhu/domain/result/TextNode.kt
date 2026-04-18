package top.emilejones.hhu.domain.result

import top.emilejones.hhu.domain.AggregateRoot
import java.util.*

/**
 * 文本节点，承载切分后的文本内容和层级信息。
 * @author EmileJones
 */
data class TextNode(
    override val id: String,
    val fileNodeId: String,
    val text: String,
    val seq: Int,
    val level: Int,
    val type: TextType,
    val isEmbedded: Boolean,
    val vector: List<Float>?,
    val summary: String? = null
) : AggregateRoot<String>(id) {

    companion object {
        /**
         * 创建文本节点。
         */
        fun create(
            fileNodeId: String,
            text: String,
            seq: Int,
            level: Int,
            type: TextType,
            summary: String? = null,
            vector: List<Float>? = null
        ): TextNode {
            return TextNode(
                id = UUID.randomUUID().toString(),
                fileNodeId = fileNodeId,
                text = text,
                seq = seq,
                level = level,
                type = type,
                isEmbedded = vector != null,
                vector = vector,
                summary = summary
            )
        }
    }
}
