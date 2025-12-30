package top.emilejones.hhu.domain.document

import top.emilejones.hhu.domain.AggregateRoot

/**
 * 原始文档目录。
 * @author EmileJones
 */
class Catalog(
    override val id: String,
    val name: String
): AggregateRoot<String>(id) {
}
