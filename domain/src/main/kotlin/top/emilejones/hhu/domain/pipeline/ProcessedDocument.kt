package top.emilejones.hhu.domain.pipeline

class ProcessedDocument(
    val id: String,
    val name: String,
    val sourceDocumentId: String,
    val filePath: String,
    val type: ProcessedDocumentType
) {
}