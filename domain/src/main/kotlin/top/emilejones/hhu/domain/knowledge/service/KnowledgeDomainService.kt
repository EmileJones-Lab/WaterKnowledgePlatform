package top.emilejones.hhu.domain.knowledge.service


import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType
import java.time.Instant

/**
 * 知识库相关领域服务，承担跨聚合的业务编排与校验。
 * @author EmileJones
 */
class KnowledgeDomainService {
    /**
     * 添加知识文件到知识库中，如果违规操作会抛出异常。
     * 负责校验业务规则并返回绑定时间。
     * @param knowledgeDocument 知识文件
     * @param knowledgeCatalog 知识库
     * @return Instant 绑定成功的时间戳
     */
    fun bindKnowledgeDocumentToKnowledgeCatalog(
        knowledgeDocument: KnowledgeDocument,
        knowledgeCatalog: KnowledgeCatalog
    ): Instant {
        if (knowledgeCatalog.type == KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR) {
            require(knowledgeDocument.type == KnowledgeDocumentType.STRUCTURE_SPLITTER) { "结构化知识库只能存储结构化内容" }
        }
        
        return Instant.now()
    }
}
