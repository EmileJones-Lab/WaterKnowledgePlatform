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
     */
    fun findAll(): List<KnowledgeCatalog>

    /**
     * 根据标识查询目录。
     */
    fun find(knowledgeCatalogId: String): KnowledgeCatalog?

    /**
     * 保存知识库信息；若存在同标识记录时覆盖旧内容（upsert 行为）。
     */
    fun save(knowledgeCatalog: KnowledgeCatalog)

    /**
     * 将一个知识文件添加到知识库中；已存在则覆盖绑定关系。
     */
    fun bind(knowledgeDocument: KnowledgeDocument, knowledgeCatalog: KnowledgeCatalog)
}
