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
     * @return 一个success的信息，这里可不可以考虑封装一个Result类？
     * @author EmileNathon
     */
    fun save(knowledgeCatalog: KnowledgeCatalog)

    /**
     * 将一个知识文件添加到知识库中；已存在则覆盖绑定关系。
     * @param knowledgeDocument 知识库文档
     * @param knowledgeCatalog 知识库目录
     * @return 一个success的信息，这里可不可以考虑封装一个Result类？
     * @author EmileNathon
     */
    fun bind(knowledgeDocument: KnowledgeDocument, knowledgeCatalog: KnowledgeCatalog)
}
