package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.AggregateRoot

class KnowledgeCatalog(
    override val id: String,
    val name: String,
    val milvusCollectionName: String,
    val type: KnowledgeCatalogType
): AggregateRoot<String>(id) {

}