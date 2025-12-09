package top.emilejones.hhu.domain.knowledge.infrastructure

import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument

/**
 * 知识库目录的存储接口。
 * @author EmileJones
 */
interface KnowledgeCatalogRepository {
    /**
     * 查询全部知识库目录。
     * @param null
     * @return List<KnowledgeCatalog> 知识库目录的集合
     * @author EmileNathon
     */
    fun findAll(): List<KnowledgeCatalog>

    /**
     * 根据标识查询目录。
     * @param knowledgeCatalogId 知识库目录的id
     * @return KnowledgeCatalog 知识库目录（可以为空，即不存在）
     * @author EmileNathon
     */
    fun find(knowledgeCatalogId: String): KnowledgeCatalog?

    /**
     * 保存知识库信息；若存在同标识记录时覆盖旧内容（upsert 行为）。
     * @param knowledgeCatalog 知识库目录的一个实例
     * @author EmileNathon
     */
    fun save(knowledgeCatalog: KnowledgeCatalog)

    /**
     * 将一个知识文件添加到知识库中；已存在则不操作。
     * @param knowledgeDocument 知识库文档
     * @param knowledgeCatalog 知识库目录
     * @author EmileNathon
     */
    fun bind(knowledgeDocument: KnowledgeDocument, knowledgeCatalog: KnowledgeCatalog)

    /**
     * 删除指定的知识库
     * @param knowledgeCatalogId
     */
    fun delete(knowledgeCatalogId: String)

    /**
     * 批量化删除对应知识库中的向量化文件
     * @param knowledgeCatalogId 知识库id
     * @param knowledgeDocumentIdList 向量化文件id集合
     */
    fun deleteKnowledgeDocumentFromKnowledgeCatalog(knowledgeCatalogId: String, knowledgeDocumentIdList: List<String>)
}
