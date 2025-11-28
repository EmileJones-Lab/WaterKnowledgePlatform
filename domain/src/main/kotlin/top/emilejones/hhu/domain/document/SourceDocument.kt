package top.emilejones.hhu.domain.document

import top.emilejones.hhu.domain.AggregateRoot

class SourceDocument(
    override val id: String,
    val name: String,
    val catalogId: String,
    val filePath: String,
    val type: SourceFileType
): AggregateRoot<String>(id) {
}