package top.emilejones.hhu.domain.document

class SourceDocument(
    val id: String,
    val name: String,
    val catalogId: String,
    val filePath: String,
    val type: SourceFileType
) {
}