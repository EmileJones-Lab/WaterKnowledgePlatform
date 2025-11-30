package top.emilejones.hhu.domain.pipeline

import top.emilejones.hhu.domain.AggregateRoot
import java.time.Instant

class MarkdownDocument(
    override val id: String,
    val sourceDocumentId: String,
    val filePath: String,
    val createTime: Instant
) : AggregateRoot<String>(id) {

    companion object {
        fun create(id: String, sourceDocumentId: String, filePath: String): MarkdownDocument {
            return MarkdownDocument(id, sourceDocumentId, filePath, Instant.now())
        }
    }
}