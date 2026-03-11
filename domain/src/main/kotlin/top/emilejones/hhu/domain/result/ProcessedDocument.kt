package top.emilejones.hhu.domain.result

import top.emilejones.hhu.domain.AggregateRoot
import java.time.Instant
import java.util.*

/**
 * OCR 输出的 Markdown 文档记录。
 * @author EmileJones
 */
class ProcessedDocument(
    override val id: String,
    val sourceDocumentId: String,
    val filePath: String,
    val createTime: Instant,
    val processedDocumentType: ProcessedDocumentType
) : AggregateRoot<String>(id) {

    companion object {
        /**
         * 创建带时间戳的 Markdown 文档。
         */
        fun create(sourceDocumentId: String, filePath: String, type: ProcessedDocumentType): ProcessedDocument {
            return ProcessedDocument(UUID.randomUUID().toString(), sourceDocumentId, filePath, Instant.now(), type)
        }
    }
}
