package top.emilejones.hhu.domain.pipeline

import top.emilejones.hhu.domain.AggregateRoot

/**
 * 文件节点，承载处理后的文件与其嵌入状态。
 * @author EmileJones
 */
class FileNode(
    override val id: String,
    val sourceDocumentId: String,
    var isEmbedded: Boolean,
    val childNodeNumber: Int
): AggregateRoot<String>(id) {
}
