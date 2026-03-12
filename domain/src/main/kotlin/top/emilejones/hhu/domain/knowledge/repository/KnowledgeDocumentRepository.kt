package top.emilejones.hhu.domain.knowledge.repository

import top.emilejones.hhu.domain.framwork.ConsistentDataProcessor
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument
import top.emilejones.hhu.domain.knowledge.repository.dto.KnowledgeDocumentWithBindTime
import kotlin.ReplaceWith

/**
 * 知识文档持久化接口，抽象数据存取行为。
 * @author EmileJones
 */
interface KnowledgeDocumentRepository: ConsistentDataProcessor<String, KnowledgeDocument> {
    /**
     * 分页查询指定知识库下的文档。
     * @param knowledgeCatalogId 知识库目录id
     * @param limit 每页数量
     * @param offset 偏移量，从哪里开始查
     * @return List<KnowledgeDocument> 向量化文件的集合
     * @author EmileNathon
     */
    @Deprecated(message = "由于通过CatalogId查询出的Document会有相关的属性（例如bindTime等），但KnowledgeDocument无法承载这些属性，所以弃用这个方法。",
        replaceWith = ReplaceWith("findDocumentsWithBindInfoByCatalogId"))
    fun findByKnowledgeCatalogId(knowledgeCatalogId: String, limit: Int, offset: Int): List<KnowledgeDocument>

    /**
     * 分页查询指定知识库下的文档，并包含加入知识库的时间。
     * @param knowledgeCatalogId 知识库目录id
     * @param limit 每页数量
     * @param offset 偏移量，从哪里开始查
     * @param keyWord 根据向量化文件名模糊查询
     * @return List<KnowledgeDocumentWithBindTime> 带有绑定时间的文档集合
     */
    fun findDocumentsWithBindInfoByCatalogId(knowledgeCatalogId: String, limit: Int, offset: Int, keyWord: String?): List<KnowledgeDocumentWithBindTime>

    /**
     * 查询可用于构建知识库的候选文档列表。
     * @param knowledgeCatalogId 知识库目录id
     * @param keyWord 根据向量化文件名模糊查询
     * @return List<KnowledgeDocument> 向量化文件的集合，这里需考虑去重
     * @author EmileNathon
     */
    fun findCandidateKnowledgeDocumentKnowledgeCatalogId(knowledgeCatalogId: String, keyWord: String?): List<KnowledgeDocument>

    /**
     * 根据向量化文件的id查询已经绑定了该文件的所有知识库
     * @param knowledgeDocumentId 向量化文件id
     * @return List<KnowledgeCatalog> 知识库集合，可以为empty 但不能为null，需要考虑去重
     */
    fun findKnowledgeCatalogByKnowledgeDocumentId(knowledgeDocumentId: String): List<KnowledgeCatalog>

    /**
     * 根据向量化任务 ID 查询对应的知识文档。
     * @param embeddingMissionId 向量化任务 ID
     * @return KnowledgeDocument? 知识文档，若未查询到则返回 null
     * @author EmileJones
     */
    fun findByEmbeddingMissionId(embeddingMissionId: String): KnowledgeDocument?
}
