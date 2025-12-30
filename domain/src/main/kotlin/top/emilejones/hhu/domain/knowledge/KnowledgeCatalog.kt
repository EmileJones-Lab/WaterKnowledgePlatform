package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.AggregateRoot
import java.time.Instant
import top.emilejones.hhu.domain.knowledge.event.CreatedKnowledgeCatalogEvent


/**
 * 知识库目录，描述向量集合的基本信息。
 * @author EmileJones
 */
data class KnowledgeCatalog(
    override val id: String,
    val name: String,
    val milvusCollectionName: String,
    val createTime: Instant,
    val type: KnowledgeCatalogType
) : AggregateRoot<String>(id) {
    companion object {
        fun create(
            id: String,
            name: String,
            milvusCollectionName: String,
            type: KnowledgeCatalogType
        ): KnowledgeCatalog {
            val knowledgeCatalog = KnowledgeCatalog(id, name, milvusCollectionName, Instant.now(), type)
            knowledgeCatalog.raiseEvent(CreatedKnowledgeCatalogEvent(knowledgeCatalog))
            return knowledgeCatalog
        }
    }
}
