package top.emilejones.hhu.domain.knowledge.infrastructure

import top.emilejones.hhu.domain.framwork.ConsistentDataProcessor
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument
import java.time.Instant

/**
 * 知识库目录的存储接口。
 * @author EmileJones
 */
interface KnowledgeCatalogRepository : ConsistentDataProcessor<String, KnowledgeCatalog> {
    /**
     * 查询所有的知识库信息。
     * @return List<KnowledgeCatalog> 知识库目录的集合，可以是空列表但不会为null。
     */
    fun findAll(): MutableList<KnowledgeCatalog?>

    /**
     * 将一个知识文件添加到知识库中；已存在则不操作。
     * @param knowledgeDocument 知识库文档
     * @param knowledgeCatalog 知识库目录
     * @param bindTime 绑定时间（业务发生时间）
     * @author EmileNathon
     */
    fun bind(knowledgeDocument: KnowledgeDocument, knowledgeCatalog: KnowledgeCatalog, bindTime: Instant)

    /**
     * 批量化删除对应知识库中的向量化文件
     * @param knowledgeCatalogId 知识库id
     * @param knowledgeDocumentIdList 向量化文件id集合
     */
    fun deleteKnowledgeDocumentFromKnowledgeCatalog(knowledgeCatalogId: String, knowledgeDocumentIdList: List<String>)
}
