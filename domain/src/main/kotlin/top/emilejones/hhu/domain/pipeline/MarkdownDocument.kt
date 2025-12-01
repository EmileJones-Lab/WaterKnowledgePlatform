package top.emilejones.hhu.domain.pipeline

import top.emilejones.hhu.domain.AggregateRoot
import java.time.Instant

/**
 * OCR 输出的 Markdown 文档记录。
 * @author EmileJones
 */
class MarkdownDocument(
    override val id: String,
    val sourceDocumentId: String,
    val filePath: String,
    val createTime: Instant
) : AggregateRoot<String>(id) {

    companion object {
        /**
         * 创建带时间戳的 Markdown 文档。
         */
        fun create(id: String, sourceDocumentId: String, filePath: String): MarkdownDocument {
            return MarkdownDocument(id, sourceDocumentId, filePath, Instant.now())
        }
    }
}
