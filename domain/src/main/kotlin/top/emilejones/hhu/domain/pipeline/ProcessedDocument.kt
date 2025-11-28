package top.emilejones.hhu.domain.pipeline

import top.emilejones.hhu.domain.AggregateRoot

class ProcessedDocument(
    override val id: String,
    val name: String,
    val sourceDocumentId: String,
    val filePath: String,
    val type: ProcessedDocumentType
): AggregateRoot<String>(id) {
}