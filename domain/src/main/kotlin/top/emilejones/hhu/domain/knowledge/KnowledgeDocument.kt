package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.AggregateRoot

/**
 * 知识库中的文档元数据，关联具体的向量化任务。
 * @author EmileJones
 */
class KnowledgeDocument(
    override val id: String,
    val name: String,
    val embeddingMissionId: String,
    val type: KnowledgeDocumentType,
) : AggregateRoot<String>(id)
