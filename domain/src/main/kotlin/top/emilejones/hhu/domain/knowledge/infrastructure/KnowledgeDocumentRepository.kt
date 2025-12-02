package top.emilejones.hhu.domain.knowledge.infrastructure

import top.emilejones.hhu.domain.knowledge.KnowledgeDocument

/**
 * 知识文档持久化接口，抽象数据存取行为。
 * @author EmileJones
 */
interface KnowledgeDocumentRepository {
    /**
     * 分页查询指定知识库下的文档。
     * @param knowledgeCatalogId 知识库目录id
     * @param limit 每页数量
     * @param offset 偏移量，从哪里开始查
     * @return List<KnowledgeDocument> 知识库的集合
     * @author EmileNathon
     */
    fun findByKnowledgeCatalogId(knowledgeCatalogId: String, limit: Int, offset: Int): List<KnowledgeDocument>

    /**
     * 查询可用于构建知识库的候选文档列表。
     * @param knowledgeCatalogId 知识库目录id
     * @return List<KnowledgeDocument> 知识库的集合，这里需考虑去重
     * @author EmileNathon
     */
    fun findCandidateKnowledgeDocumentKnowledgeCatalogId(knowledgeCatalogId: String): List<KnowledgeDocument>

    /**
     * 保存或更新知识文档；若已存在相同标识的记录，将覆盖旧内容（等同于 upsert）。
     * @param knowledgeDocument 知识库文档
     * @return 一个success的信息，这里可不可以考虑封装一个Result类？
     * @author EmileNathon
     */
    fun save(knowledgeDocument: KnowledgeDocument)

    /**
     * 删除指定知识文档。
     * @param knowledgeDocumentId 知识库文档id
     * @return 一个success的信息，这里可不可以考虑封装一个Result类？
     * @author EmileNathon
     */
    fun delete(knowledgeDocumentId: String)
}
