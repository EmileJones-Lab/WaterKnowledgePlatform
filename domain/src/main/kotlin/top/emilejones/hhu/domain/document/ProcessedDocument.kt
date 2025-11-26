package top.emilejones.hhu.domain.document

class ProcessedDocument(
    val id: String,
    val name: String,
    val sourceDocumentId: String,
    val filePath: String,
    val type: ProcessedDocumentType
) {
}