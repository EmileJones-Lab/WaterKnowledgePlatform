package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.AggregateRoot

/**
 * 知识库目录，描述向量集合的基本信息。
 * @author EmileJones
 */
class KnowledgeCatalog(
    override val id: String,
    val name: String,
    val milvusCollectionName: String,
    val type: KnowledgeCatalogType
): AggregateRoot<String>(id)
