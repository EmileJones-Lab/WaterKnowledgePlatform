package top.emilejones.hhu.domain.document

import top.emilejones.hhu.domain.AggregateRoot

/**
 * 源文件记录，描述原始上传的文档。
 * @author EmileJones
 */
class SourceDocument(
    override val id: String,
    val name: String,
    val catalogId: String,
    val filePath: String,
    val type: SourceFileType
): AggregateRoot<String>(id) {
}
