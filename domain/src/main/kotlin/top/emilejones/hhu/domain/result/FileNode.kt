package top.emilejones.hhu.domain.result

import top.emilejones.hhu.domain.AggregateRoot
import java.util.*

/**
 * 文件节点，承载处理后的文件与其嵌入状态。
 * @author EmileJones
 */
class FileNode(
    override val id: String,
    val sourceDocumentId: String,
    var isEmbedded: Boolean
) : AggregateRoot<String>(id) {
    companion object {
        fun create(sourceDocumentId: String): FileNode {
            return FileNode(
                id = UUID.randomUUID().toString(),
                sourceDocumentId = sourceDocumentId,
                isEmbedded = false
            )
        }
    }
}
