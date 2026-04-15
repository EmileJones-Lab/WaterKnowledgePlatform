package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.AggregateRoot
import java.time.Instant
import java.util.*


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
            name: String,
            milvusCollectionName: String,
            type: KnowledgeCatalogType
        ): KnowledgeCatalog {
            val knowledgeCatalog =
                KnowledgeCatalog(UUID.randomUUID().toString(), name, milvusCollectionName, Instant.now(), type)
            return knowledgeCatalog
        }
    }
}
