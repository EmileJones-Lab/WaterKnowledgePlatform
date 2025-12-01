package top.emilejones.hhu.domain.pipeline

import top.emilejones.hhu.domain.AggregateRoot

class FileNode(
    val elementId: String,
    val sourceDocumentId: String,
    var isEmbedded: Boolean,
    val childNodeNumber: Int
): AggregateRoot<String>(elementId) {
}
