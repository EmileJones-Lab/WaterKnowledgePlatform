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
    isEmbedded: Boolean,
    fileAbstract: String? = null
) : AggregateRoot<String>(id) {
    var fileAbstract: String? = fileAbstract
        private set
    var isEmbedded: Boolean = isEmbedded
        private set

    fun saveFileAbstract(fileAbstract: String) {
        require(!isEmbedded) { "FileNode[$id]节点已经被向量化" }
        require(this.fileAbstract == null) { "FileNode[$id]节点摘要已经存在" }
        this.fileAbstract = fileAbstract
        this.isEmbedded = true
    }

    fun markAsEmbedded() {
        require(!isEmbedded) { "FileNode[$id]节点已经被向量化" }
        this.isEmbedded = true
    }

    companion object {
        fun create(sourceDocumentId: String, fileAbstract: String? = null): FileNode {
            return FileNode(
                id = UUID.randomUUID().toString(),
                sourceDocumentId = sourceDocumentId,
                isEmbedded = false,
                fileAbstract = fileAbstract
            )
        }
    }
}
