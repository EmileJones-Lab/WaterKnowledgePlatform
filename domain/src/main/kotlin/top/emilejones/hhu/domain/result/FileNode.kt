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
    fileAbstract: String? = null,
    vector: List<Float>? = null
) : AggregateRoot<String>(id) {
    var fileAbstract: String? = fileAbstract
        private set
    var isEmbedded: Boolean = isEmbedded
        private set
    var vector: List<Float>? = vector
        private set

    fun saveFileAbstract(fileAbstract: String) {
        require(this.fileAbstract == null) { "FileNode[$id]节点摘要已经存在" }
        this.fileAbstract = fileAbstract
    }

    /**
     * 保存文件节点的向量信息。
     * 
     * @param vector 向量数据
     */
    fun saveVector(vector: List<Float>) {
        this.vector = vector
        this.isEmbedded = true
    }

    fun markAsEmbedded() {
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
