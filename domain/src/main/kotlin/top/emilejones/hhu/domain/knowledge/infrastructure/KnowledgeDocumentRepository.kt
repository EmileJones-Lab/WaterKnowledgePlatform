package top.emilejones.hhu.domain.knowledge.infrastructure

import top.emilejones.hhu.domain.knowledge.KnowledgeDocument

/**
 * 知识文档持久化接口，抽象数据存取行为。
 * @author EmileJones
 */
interface KnowledgeDocumentRepository {
    /**
     * 分页查询指定知识库下的文档。
     */
    fun findByKnowledgeCatalogId(knowledgeCatalogId: String, limit: Int, offset: Int): List<KnowledgeDocument>

    /**
     * 查询可用于构建知识库的候选文档列表。
     */
    fun findCandidateKnowledgeDocumentKnowledgeCatalogId(knowledgeCatalogId: String): List<KnowledgeDocument>

    /**
     * 保存或更新知识文档；若已存在相同标识的记录，将覆盖旧内容（等同于 upsert）。
     */
    fun save(knowledgeDocument: KnowledgeDocument)

    /**
     * 删除指定知识文档。
     */
    fun delete(knowledgeDocumentId: String)
}
