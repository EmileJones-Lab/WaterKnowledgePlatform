package top.emilejones.hhu.domain.pipeline

class FileNode(
    val elementId: String,
    val sourceDocumentId: String,
    var isEmbedded: Boolean,
    val childNodeNumber: Int
) {
}
