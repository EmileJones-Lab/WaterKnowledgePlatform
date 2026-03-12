package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.AggregateRoot
import java.time.Instant
import java.util.UUID

/**
 * 知识库中的文档元数据，关联具体的向量化任务。
 * @author EmileJones
 */
data class KnowledgeDocument(
    override val id: String,
    val name: String,
    val embeddingMissionId: String,
    val type: KnowledgeDocumentType,
    val createTime: Instant
) : AggregateRoot<String>(id) {
    companion object {
        fun create(
            name: String,
            embeddingMissionId: String,
            type: KnowledgeDocumentType
        ): KnowledgeDocument {
            return KnowledgeDocument(
                UUID.randomUUID().toString(),
                name,
                embeddingMissionId,
                type,
                Instant.now()
            )
        }
    }
}
